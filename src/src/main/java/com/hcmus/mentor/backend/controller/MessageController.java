package com.hcmus.mentor.backend.controller;

import com.corundumstudio.socketio.SocketIOServer;
import com.hcmus.mentor.backend.controller.payload.request.*;
import com.hcmus.mentor.backend.controller.payload.request.meetings.ForwardRequest;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.UpdateMessageResponse;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.MessageRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.CurrentUser;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.GroupService;
import com.hcmus.mentor.backend.service.MessageService;
import com.hcmus.mentor.backend.service.NotificationService;
import com.hcmus.mentor.backend.service.SocketIOService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Message controllers.
 */
@Tag(name = "message")
@RequestMapping("api/messages")
@SecurityRequirement(name = "bearer")
@RestController
@RequiredArgsConstructor
@Validated
public class MessageController {

    private final MessageService messageService;
    private final GroupService groupService;
    private final SocketIOServer socketServer;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MessageRepository messageRepository;
    private final SocketIOService socketIOService;
    private final GroupRepository groupRepository;
    private final ChannelRepository channelRepository;

    /**
     * Get messages of group.
     *
     * @param groupId GroupId.
     * @param page    Page number to return.
     * @param size    Required page size (amount of items returned at a time).
     * @return ResponseEntity with a list of message details.
     */
    @GetMapping(value = {""})
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<List<MessageDetailResponse>> getGroupMessages(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam String groupId,
            @RequestParam int page,
            @RequestParam int size) {
        String userId = customerUserDetails.getId();
        if (!groupService.isGroupMember(groupId, userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(messageService.getGroupMessages(userId, groupId, page, size));
    }

    /**
     * Delete a message in a group or channel.
     *
     * @param messageId The ID of the message to be deleted.
     * @return ResponseEntity indicating success or failure.
     */
    @DeleteMapping("{messageId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    @ApiResponse(responseCode = "404", description = "Message not found")
    public ResponseEntity<Void> deleteMessage(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String messageId) {
        Optional<Message> messageWrapper = messageRepository.findById(messageId);
        if (messageWrapper.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Message message = messageWrapper.get();

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (groupWrapper.isEmpty()) {
            Optional<Channel> channelWrapper = channelRepository.findById(message.getGroupId());
            if (channelWrapper.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Channel channel = channelWrapper.get();
            channel.unpinMessage(messageId);
            channelRepository.save(channel);
        } else {
            Group group = groupWrapper.get();
            if (!group.isMember(customerUserDetails.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            group.unpinMessage(message.getId());
            groupRepository.save(group);
        }

        if (!customerUserDetails.getId().equals(message.getSenderId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (message.isDeleted()) {
            return ResponseEntity.accepted().build();
        }
        message.delete();
        messageRepository.save(message);

        UpdateMessageResponse response = UpdateMessageResponse.builder()
                .messageId(message.getId())
                .newContent("")
                .action(UpdateMessageResponse.Action.delete)
                .build();
        socketIOService.sendUpdateMessage(response, message.getGroupId());

        return ResponseEntity.ok().build();
    }

    /**
     * Edit a message in a group.
     *
     * @param request The request containing information for editing the message.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping("edit")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Void> editMessage(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody EditMessageRequest request) {
        Optional<Message> messageWrapper = messageRepository.findById(request.getMessageId());
        if (!messageWrapper.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Message message = messageWrapper.get();
        if (!groupService.isGroupMember(message.getGroupId(), customerUserDetails.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!customerUserDetails.getId().equals(message.getSenderId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        message.edit(request);
        messageRepository.save(message);

        UpdateMessageResponse response =
                UpdateMessageResponse.builder()
                        .messageId(message.getId())
                        .newContent(message.getContent())
                        .action(UpdateMessageResponse.Action.update)
                        .build();
        socketIOService.sendUpdateMessage(response, message.getGroupId());

        return ResponseEntity.ok().build();
    }

    /**
     * Send a file message
     *
     * @param id       The ID of the file message.
     * @param groupId  The ID of the group to which the file is being sent.
     * @param senderId The ID of the sender of the file.
     * @param file     The file being sent.
     * @return ResponseEntity with the URL of the sent file.
     */
    @PostMapping(value = "file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<String> sendFile(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam String id,
            @RequestParam String groupId,
            @RequestParam String senderId,
            @RequestPart(required = false) MultipartFile file) {
        SendFileRequest request = SendFileRequest.builder()
                .id(id)
                .groupId(groupId)
                .senderId(customerUserDetails.getId())
                .file(file)
                .build();
        Message message = messageService.saveFileMessage(request);
        User sender = userRepository.findById(message.getSenderId()).orElse(null);

        MessageDetailResponse response = MessageDetailResponse.from(message, sender);
        socketServer.getRoomOperations(groupId).sendEvent("receive_message", response);
        notificationService.sendNewMediaMessageNotification(response);

        return ResponseEntity.ok(message.getFile().getUrl());
    }

    /**
     * Find messages of a group (Paging).
     *
     * @param groupId The ID of the group to search for messages.
     * @param query   The query string to search for in messages.
     * @param page    Page number for pagination.
     * @param size    Required page size (amount of items returned at a time).
     * @return ResponseEntity with a list of message responses.
     */
    @GetMapping("find")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<List<MessageResponse>> findGroupMessages(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam String groupId,
            @RequestParam String query,
            @RequestParam int page,
            @RequestParam int size) {
        String userId = customerUserDetails.getId();
        if (!groupService.isGroupMember(groupId, userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(messageService.findGroupMessagesByText(groupId, query, page, size));
    }

    /**
     * Send an image message.
     *
     * @param id       The ID of the message.
     * @param groupId  The ID of the group to which the image is being sent.
     * @param senderId The ID of the sender of the image.
     * @param files    The array of image files being sent.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Void> sendImages(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam String id,
            @RequestParam String groupId,
            @RequestParam String senderId,
            @RequestPart(required = false) MultipartFile[] files) {
        SendImagesRequest request = SendImagesRequest.builder()
                .id(id)
                .groupId(groupId)
                .senderId(customerUserDetails.getId())
                .files(files)
                .build();
        Message message = messageService.saveImageMessage(request);
        User sender = userRepository.findById(message.getSenderId()).orElse(null);

        MessageDetailResponse response = MessageDetailResponse.from(message, sender);
        socketServer.getRoomOperations(groupId).sendEvent("receive_message", response);
        notificationService.sendNewMediaMessageNotification(response);

        return ResponseEntity.ok().build();
    }

    /**
     * Mention users in a message and send notifications to the specified receivers.
     *
     * @param command Command for mentioning users in a message.
     * @return A ResponseEntity indicating the success of the operation.
     */
    @PostMapping("mention")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Void> mentionUser(
            @RequestBody MentionUserCommand command) {
        var message = messageService.find(command.getMessageId());
        for (var receiverId : command.getReceiverIds()) {
            var receiver = userRepository.findById(receiverId).orElse(null);
            if (receiver == null) {
                continue;
            }
            var messageDetail = MessageDetailResponse.from(message, receiver);

            notificationService.sendNewMessageNotification(messageDetail);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * React to messages of a group.
     *
     * @param request The request containing reaction details.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping("react")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Void> react(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody @Valid ReactMessageRequest request) {
        messageService.reactMessage(request);

        return ResponseEntity.ok().build();
    }

    /**
     * Remove all reactions on message.
     *
     * @param messageId The ID of the message from which reactions should be removed.
     * @param senderId  The ID of the sender whose reactions will be removed.
     * @return ResponseEntity indicating success or failure.
     */
    @DeleteMapping("react")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Void> removeReact(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam String messageId,
            @RequestParam String senderId) {
        messageService.removeReaction(messageId, senderId);
        return ResponseEntity.ok().build();
    }

    /**
     * Forward a message to another group.
     *
     * @param customerUserDetails The current user's principal information.
     * @param request             The request payload for forwarding the message.
     * @return ResponseEntity indicating the success of the forward operation.
     */
    @PostMapping("forward")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Void> forwardMessage(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody ForwardRequest request) {
        messageService.saveForwardMessage(customerUserDetails.getId(), request);
        return ResponseEntity.ok().build();
    }
}

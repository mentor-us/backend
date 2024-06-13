package com.hcmus.mentor.backend.controller;

import com.corundumstudio.socketio.SocketIOServer;
import com.hcmus.mentor.backend.controller.payload.request.*;
import com.hcmus.mentor.backend.controller.payload.request.meetings.ForwardRequest;
import com.hcmus.mentor.backend.controller.payload.request.messages.EditMessageRequest;
import com.hcmus.mentor.backend.controller.payload.request.messages.ReactMessageRequest;
import com.hcmus.mentor.backend.controller.payload.request.messages.SendFileRequest;
import com.hcmus.mentor.backend.controller.payload.request.messages.SendImagesRequest;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.UpdateMessageResponse;
import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.MessageRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.CurrentUser;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    private final static Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final MessageService messageService;
    private final GroupService groupService;
    private final SocketIOServer socketServer;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MessageRepository messageRepository;
    private final SocketIOService socketIOService;
    private final ChannelRepository channelRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final PermissionService permissionService;

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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var response = messageService.getGroupMessages(userId, groupId, page, size);

        return ResponseEntity.ok(response);
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
            @PathVariable String messageId) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var message = messageRepository.findById(messageId).orElse(null);
        if (message == null) {
            return ResponseEntity.notFound().build();
        }

        var channel = channelRepository.findById(message.getChannel().getId()).orElse(null);
        if (channel == null) {
            return ResponseEntity.badRequest().build();
        }

        if (!message.getSender().getId().equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (message.isDeleted()) {
            return ResponseEntity.accepted().build();
        }

        message.delete();
        messageRepository.save(message);

        if (channel.getMessagesPinned().contains(message)) {
            channel.getMessagesPinned().remove(message);
            channel.ping();

            channel = channelRepository.save(channel);
            logger.info("Remove pinned message with id {}, pinned message remaining {}", message.getId(), channel.getMessagesPinned().toArray().length);
        }
        var response = UpdateMessageResponse.builder()
                .messageId(message.getId())
                .newContent("")
                .action(UpdateMessageResponse.Action.delete)
                .build();
        socketIOService.sendUpdateMessage(response, message.getChannel().getId());

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
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var message = messageRepository.findById(request.getMessageId()).orElse(null);
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        if (!permissionService.isMemberInChannel(message.getChannel().getId(), currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!message.getSender().getId().equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        message.edit(request);
        messageRepository.save(message);

        UpdateMessageResponse response = UpdateMessageResponse.builder()
                .messageId(message.getId())
                .newContent(message.getContent())
                .action(UpdateMessageResponse.Action.update)
                .build();
        socketIOService.sendUpdateMessage(response, message.getChannel().getId());

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

        MessageDetailResponse response = messageService.mappingToMessageDetailResponse(message, senderId);
        socketServer.getRoomOperations(groupId).sendEvent("receive_message", response);
        notificationService.sendForMediaMessage(message);

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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
        MessageDetailResponse response = messageService.mappingToMessageDetailResponse(message, senderId);
        socketServer.getRoomOperations(groupId).sendEvent("receive_message", response);
        notificationService.sendForMediaMessage(message);

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

            notificationService.sendForMessage(message);
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
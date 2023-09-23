package com.hcmus.mentor.backend.controller;

import com.corundumstudio.socketio.SocketIOServer;
import com.hcmus.mentor.backend.entity.Channel;
import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.entity.Message;
import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.payload.request.EditMessageRequest;
import com.hcmus.mentor.backend.payload.request.ReactMessageRequest;
import com.hcmus.mentor.backend.payload.request.SendFileRequest;
import com.hcmus.mentor.backend.payload.request.SendImagesRequest;
import com.hcmus.mentor.backend.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.payload.response.messages.UpdateMessageResponse;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.MessageRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.GroupService;
import com.hcmus.mentor.backend.service.MessageService;
import com.hcmus.mentor.backend.service.NotificationService;
import com.hcmus.mentor.backend.service.SocketIOService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

@Tag(name = "Message APIs", description = "REST APIs for message collections")
@RestController
@RequestMapping("/api/messages")
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

    public MessageController(MessageService messageService, GroupService groupService, SocketIOServer socketServer, UserRepository userRepository, NotificationService notificationService, MessageRepository messageRepository, SocketIOService socketIOService, GroupRepository groupRepository, ChannelRepository channelRepository) {
        this.messageService = messageService;
        this.groupService = groupService;
        this.socketServer = socketServer;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.messageRepository = messageRepository;
        this.socketIOService = socketIOService;
        this.groupRepository = groupRepository;
        this.channelRepository = channelRepository;
    }

    @Operation(summary = "Get messages of group", description = "Retrieve message by paging to display on Chat UI", tags = "Message APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieve successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping(value = {"/", ""})
    public ResponseEntity<List<MessageDetailResponse>> getGroupMessages(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                                           @RequestParam String groupId, @RequestParam int page, @RequestParam int size) {
        String userId = userPrincipal.getId();
        if (!groupService.isGroupMember(groupId, userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(messageService.getGroupMessages(userId, groupId, page, size));
    }

    @Operation(summary = "React messages of group", description = "React message", tags = "Message APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "React successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @PostMapping("/react")
    public ResponseEntity<Void> react(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                      @RequestBody @Valid ReactMessageRequest request) {
        messageService.reactMessage(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove all reactions on message", description = "Remove all reactions message", tags = "Message APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Remove successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @DeleteMapping("/react")
    public ResponseEntity<Void> removeReact(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                            @RequestParam String messageId,
                                            @RequestParam String senderId) {
        messageService.removeReaction(messageId, senderId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Send an image message", description = "Send multiple images on Chat UI", tags = "Message APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Send successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> sendImages(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                           @RequestParam String id,
                                           @RequestParam String groupId,
                                           @RequestParam String senderId,
                                           @RequestParam(value = "files", required = false) MultipartFile[] files) throws GeneralSecurityException, IOException {
        SendImagesRequest request = SendImagesRequest.builder()
                .id(id)
                .groupId(groupId)
                .senderId(userPrincipal.getId())
                .files(files)
                .build();
        Message message = messageService.saveImageMessage(request);
        User sender = userRepository.findById(message.getSenderId()).orElse(null);

        MessageDetailResponse response = MessageDetailResponse.from(message, sender);
        socketServer.getRoomOperations(groupId)
                .sendEvent("receive_message", response);
        notificationService.sendNewMediaMessageNotification(response);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Send an file message",
            description = "Send a file on Chat UI", tags = "Message APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Send successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> sendFile(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                           @RequestParam String id,
                                           @RequestParam String groupId,
                                           @RequestParam String senderId,
                                           @RequestParam(value = "file", required = false) MultipartFile file)
            throws GeneralSecurityException, IOException {
        SendFileRequest request = SendFileRequest.builder()
                .id(id)
                .groupId(groupId)
                .senderId(userPrincipal.getId())
                .file(file)
                .build();
        Message message = messageService.saveFileMessage(request);
        User sender = userRepository.findById(message.getSenderId()).orElse(null);

        MessageDetailResponse response = MessageDetailResponse.from(message, sender);
        socketServer.getRoomOperations(groupId)
                .sendEvent("receive_message", response);
        notificationService.sendNewMediaMessageNotification(response);

        return ResponseEntity.ok(message.getFile().getUrl());
    }

    @Operation(summary = "Find messages of group (Paging)",
            description = "Find any message mapped with query string (Paging)", tags = "Message APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Find successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @GetMapping("/find")
    public ResponseEntity<List<MessageResponse>> findGroupMessages(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                                                   @RequestParam String groupId, @RequestParam String query,
                                                                   @RequestParam int page, @RequestParam int size) {
        String userId = userPrincipal.getId();
        if (!groupService.isGroupMember(groupId, userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(messageService.findGroupMessagesByText(groupId, query, page, size));
    }

    @Operation(summary = "Edit message of group",
            description = "Edit a message on single group", tags = "Message APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Edit successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @PostMapping("/edit")
    public ResponseEntity<Void> editMessage(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                            @RequestBody EditMessageRequest request) {
        Optional<Message> messageWrapper = messageRepository
                .findById(request.getMessageId());
        if (!messageWrapper.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Message message = messageWrapper.get();
        if (!groupService.isGroupMember(message.getGroupId(), userPrincipal.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!userPrincipal.getId().equals(message.getSenderId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        message.edit(request);
        messageRepository.save(message);

        UpdateMessageResponse response = UpdateMessageResponse.builder()
                .messageId(message.getId())
                .newContent(message.getContent())
                .action(UpdateMessageResponse.Action.update)
                .build();
        socketIOService.sendUpdateMessage(response, message.getGroupId());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete message of group",
            description = "Delete a message on single group", tags = "Message APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delete successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Need authentication")
    })
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
                                            @PathVariable String messageId) {
        Optional<Message> messageWrapper = messageRepository.findById(messageId);
        if (!messageWrapper.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Message message = messageWrapper.get();

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (!groupWrapper.isPresent()) {
            Optional<Channel> channelWrapper = channelRepository.findById(message.getGroupId());
            if (!channelWrapper.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            Channel channel = channelWrapper.get();
            channel.unpinMessage(messageId);
            channelRepository.save(channel);
        } else {
            Group group = groupWrapper.get();
            if (!group.isMember(userPrincipal.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            group.unpinMessage(message.getId());
            groupRepository.save(group);
        }

        if (!userPrincipal.getId().equals(message.getSenderId())) {
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
}

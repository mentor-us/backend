package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.payload.FileModel;
import com.hcmus.mentor.backend.controller.payload.request.ReactMessageRequest;
import com.hcmus.mentor.backend.controller.payload.request.SendFileRequest;
import com.hcmus.mentor.backend.controller.payload.request.SendImagesRequest;
import com.hcmus.mentor.backend.controller.payload.request.meetings.ForwardRequest;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.ReactMessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.RemoveReactionResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskAssigneeResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskMessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.service.*;
import com.hcmus.mentor.backend.service.fileupload.BlobStorage;
import io.minio.errors.*;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final GroupService groupService;
    private final MessageRepository messageRepository;
    private final MeetingRepository meetingRepository;
    private final TaskRepository taskRepository;
    private final VoteRepository voteRepository;
    private final SocketIOService socketIOService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final TaskServiceImpl taskService;
    private final BlobStorage blobStorage;
    private final ChannelRepository channelRepository;

    @Override
    public List<MessageDetailResponse> getGroupMessages(
            String viewerId, String groupId, int page, int size) {
        List<MessageResponse> responses =
                messageRepository.getGroupMessagesByGroupId(groupId, page * size, size);
        return fulfillMessages(responses, viewerId);
    }

    private List<MessageDetailResponse> fulfillMessages(
            List<MessageResponse> messages, String viewerId) {
        List<String> userIds =
                messages.stream()
                        .flatMap(response -> response.getReactions().stream())
                        .map(Reaction::getUserId)
                        .collect(Collectors.toList());
        Map<String, User> reactors =
                userRepository.findByIdIn(userIds).stream()
                        .collect(Collectors.toMap(User::getId, user -> user, (u1, u2) -> u2));
        return messages.stream()
                .map(this::fulfillMessage)
                .filter(Objects::nonNull)
                .filter(message -> !message.isDeletedAttach())
                .map(message -> fulfillReactions(message, reactors))
                .map(message -> MessageDetailResponse.totalReaction(message, viewerId))
                .collect(Collectors.toList());
    }

    private MessageDetailResponse fulfillReactions(
            MessageDetailResponse message, Map<String, User> reactors) {
        List<Reaction> reactions =
                message.getReactions().stream()
                        .map(
                                reaction -> {
                                    User reactor = reactors.getOrDefault(reaction.getUserId(), null);
                                    return fulfillReaction(reaction, reactor);
                                })
                        .filter(reaction -> reaction.getUserId() != null)
                        .collect(Collectors.toList());
        message.setReactions(reactions);
        return message;
    }

    private MessageDetailResponse fulfillMessage(MessageResponse message) {
        MessageDetailResponse response;
        switch (message.getType()) {
            case MEETING:
                response = fulfillMeetingMessage(message);
                break;
            case TASK:
                response = fulfillTaskMessage(message);
                break;
            case VOTE:
                response = fulfillVotingMessage(message);
                break;
            case TEXT:
                response = fulfillTextMessage(message);
                break;
            default:
                response = MessageDetailResponse.from(message);
        }
        return response;
    }

    private MessageDetailResponse fulfillVotingMessage(MessageResponse message) {
        Vote vote = voteRepository.findById(message.getVoteId()).orElse(null);
        if (vote != null) {
            vote.sortChoicesDesc();
        }
        return MessageDetailResponse.from(message, vote);
    }

    @Override
    public Reaction fulfillReaction(Reaction reaction, User reactor) {
        if (reactor == null) {
            return new Reaction();
        }
        reaction.update(reactor);
        return reaction;
    }

    @Override
    public MessageDetailResponse fulfillTextMessage(MessageResponse message) {
        MessageDetailResponse response = MessageDetailResponse.from(message);
        if (message.getReply() != null) {
            Optional<Message> wrapper = messageRepository.findById(message.getReply());
            if (!wrapper.isPresent()) {
                return response;
            }
            Message data = wrapper.get();
            String content = data.getContent();
            if (data.isDeleted()) {
                content = "Tin nhắn đã được xoá";
            }
            ShortProfile sender = userRepository.findShortProfile(data.getSenderId());
            String senderName = "Người dùng không tồn tại";
            if (sender != null) {
                senderName = sender.getName();
            }
            MessageDetailResponse.ReplyMessage replyMessage =
                    MessageDetailResponse.ReplyMessage.builder()
                            .id(data.getId())
                            .senderName(senderName)
                            .content(content)
                            .build();
            response.setReply(replyMessage);
        }
        return response;
    }

    @Override
    public MessageDetailResponse fulfillMeetingMessage(MessageResponse message) {
        Optional<Meeting> meeting = meetingRepository.findById(message.getMeetingId());
        return MessageDetailResponse.from(message, meeting.orElse(null));
    }

    @Override
    public MessageDetailResponse fulfillTaskMessage(MessageResponse message) {
        Optional<Task> taskWrapper = taskRepository.findById(message.getTaskId());
        if (!taskWrapper.isPresent()) {
            return null;
        }
        TaskMessageResponse taskDetail = TaskMessageResponse.from(taskWrapper.get());

        List<TaskAssigneeResponse> assignees = taskService.getTaskAssignees(taskDetail.getId());
        taskDetail.setAssignees(assignees);
        return MessageDetailResponse.from(message, taskDetail);
    }

    @Override
    public List<MessageResponse> findGroupMessagesByText(
            String groupId, String query, int page, int size) {
        List<MessageResponse> responses =
                messageRepository.findGroupMessages(groupId, query, page * size, size);
        return responses;
    }

    @Override
    public Message saveMessage(Message data) {
        groupService.pingGroup(data.getGroupId());

        return messageRepository.save(data);
    }

    @Override
    public void reactMessage(ReactMessageRequest request) {
        Optional<Message> messageWrapper = messageRepository.findById(request.getMessageId());
        if (!messageWrapper.isPresent()) {
            return;
        }

        Message message = messageWrapper.get();
        if (!groupService.isGroupMember(message.getGroupId(), request.getSenderId())) {
            return;
        }

        Emoji.Type emoji = Emoji.Type.valueOf(request.getEmojiId());
        Reaction newReaction = message.react(request.getSenderId(), emoji);
        messageRepository.save(message);

        groupService.pingGroup(message.getGroupId());

        User reactor = userRepository.findById(request.getSenderId()).orElse(null);
        ReactMessageResponse response = ReactMessageResponse.from(request, reactor);
        socketIOService.sendReact(response, message.getGroupId());
        notificationService.sendNewReactNotification(message, response, request.getSenderId());
    }

    @Override
    public void removeReaction(String messageId, String senderId) {
        Optional<Message> messageWrapper = messageRepository.findById(messageId);
        if (!messageWrapper.isPresent()) {
            return;
        }

        Message message = messageWrapper.get();
        if (!groupService.isGroupMember(message.getGroupId(), senderId)) {
            return;
        }

        message.removeReact(senderId);
        Message updatedMessage = messageRepository.save(message);

        MessageDetailResponse.TotalReaction newTotalReaction =
                calculateTotalReactionMessage(updatedMessage);
        socketIOService.sendRemoveReact(
                new RemoveReactionResponse(messageId, senderId, newTotalReaction),
                updatedMessage.getGroupId());
    }

    @Override
    public MessageDetailResponse.TotalReaction calculateTotalReactionMessage(Message message) {
        List<Emoji> data = MessageDetailResponse.generateTotalReactionData(message.getReactions());
        int total = MessageDetailResponse.calculateTotalReactionAmount(message.getReactions());
        return MessageDetailResponse.TotalReaction.builder()
                .data(data)
                .ownerReacted(Collections.emptyList())
                .total(total)
                .build();
    }

    @Override
    public Message saveImageMessage(SendImagesRequest request)
            throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<String> images = new ArrayList<>();
        for (MultipartFile multipartFile : request.getFiles()) {
            String key = blobStorage.generateBlobKey(multipartFile.getContentType());
            blobStorage.post(multipartFile, key);
            images.add(key);
        }
        Message message =
                Message.builder()
                        .id(request.getId())
                        .senderId(request.getSenderId())
                        .groupId(request.getGroupId())
                        .createdDate(new Date())
                        .type(Message.Type.IMAGE)
                        .images(images)
                        .build();
        groupService.pingGroup(request.getGroupId());
        return messageRepository.save(message);
    }

    @Override
    public Message saveFileMessage(SendFileRequest request)
            throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        var multipartFile = request.getFile();

        String key = blobStorage.generateBlobKey(multipartFile.getContentType());
        blobStorage.post(multipartFile, key);

        FileModel file =
                FileModel.builder()
                        .id(key)
                        .filename(request.getFile().getOriginalFilename())
                        .size(request.getFile().getSize())
                        .url(key)
                        .build();
        Message message =
                Message.builder()
                        .id(request.getId())
                        .senderId(request.getSenderId())
                        .groupId(request.getGroupId())
                        .createdDate(new Date())
                        .type(Message.Type.FILE)
                        .file(file)
                        .build();
        groupService.pingGroup(request.getGroupId());
        return messageRepository.save(message);
    }

    @Override
    public Message saveTaskMessage(Task task) {
        Message message =
                Message.builder()
                        .senderId(task.getAssignerId())
                        .groupId(task.getGroupId())
                        .createdDate(task.getCreatedDate())
                        .type(Message.Type.TASK)
                        .taskId(task.getId())
                        .build();

        return messageRepository.save(message);
    }

    @Override
    public Message saveVoteMessage(Vote vote) {
        Message message =
                Message.builder()
                        .senderId(vote.getCreatorId())
                        .groupId(vote.getGroupId())
                        .createdDate(new Date())
                        .type(Message.Type.VOTE)
                        .voteId(vote.getId())
                        .build();
        return messageRepository.save(message);
    }

    /**
     * @param userId String
     * @param request ForwardRequest
     */
    @Override
    public List<Message> saveForwardMessage(String userId, ForwardRequest request) {
        List<Channel> channels = channelRepository.findByIdIn(request.getChannelIds());
        Message message = messageRepository.findById(request.getMessageId()).orElse(null);

        if (message == null) {
            throw new RuntimeException("Message not found");
        }
        List<Message> messages = new ArrayList<>();
        channels.forEach(ch -> {
            Message m = Message.builder()
                    .senderId(userId)
                    .groupId(ch.getParentId())
                    .createdDate(new Date())
                    .type(Message.Type.FORWARD)
                    .content(message.getContent())
                    .reply(message.getReply())
                    .build();
            messages.add(messageRepository.save(m));
        });
        return messages;
    }
}

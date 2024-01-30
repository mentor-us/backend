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
import com.hcmus.mentor.backend.service.MessageService;
import com.hcmus.mentor.backend.service.NotificationService;
import com.hcmus.mentor.backend.service.SocketIOService;
import com.hcmus.mentor.backend.service.TaskServiceImpl;
import com.hcmus.mentor.backend.service.fileupload.BlobStorage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final ChannelRepository channelRepository;
    private final GroupRepository groupRepository;
    private final MessageRepository messageRepository;
    private final MeetingRepository meetingRepository;
    private final TaskRepository taskRepository;
    private final VoteRepository voteRepository;
    private final SocketIOService socketIOService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final TaskServiceImpl taskService;
    private final BlobStorage blobStorage;

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public Message find(String id) {
        var message = messageRepository.findById(id);

        return message.orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessageDetailResponse> getGroupMessages(
            String viewerId,
            String groupId,
            int page,
            int size) {
        List<MessageResponse> responses = messageRepository.getGroupMessagesByGroupId(groupId, page * size, size);

        return fulfillMessages(responses, viewerId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessageResponse> findGroupMessagesByText(
            String groupId, String query, int page, int size) {
        List<MessageResponse> responses =
                messageRepository.findGroupMessages(groupId, query, page * size, size);
        return responses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLastGroupMessage(String groupId) {
        Message lastMessage = messageRepository.findTopByGroupIdOrderByCreatedDateDesc(groupId).orElse(null);
        if (lastMessage == null) {
            return null;
        }
        if (Message.Status.DELETED.equals(lastMessage.getStatus())) {
            return "Tin nhắn đã được thu hồi.";
        }
        User sender = userRepository.findById(lastMessage.getSenderId()).orElse(null);
        switch (lastMessage.getType()) {
            case TEXT:
                if (sender == null) {
                    return null;
                }
                String content = lastMessage.getContent();
                return sender.getName() + ": " + Jsoup.parse(content).text();
            case FILE:
                if (sender == null) {
                    return null;
                }
                return sender.getName() + " đã gửi tệp đính kèm mới.";
            case IMAGE:
                return lastMessage.getImages().size() + " ảnh mới.";
            case VIDEO:
                break;
            case MEETING:
                return "1 lịch hẹn mới.";
            case TASK:
                return "1 công việc mới.";
            case VOTE:
                return "1 cuộc bình chọn mới.";
            case NOTIFICATION:
                return "1 thông báo mới cần phản hồi.";
            case SYSTEM:
                return "";
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reactMessage(ReactMessageRequest request) {
        Optional<Message> messageWrapper = messageRepository.findById(request.getMessageId());
        if (!messageWrapper.isPresent()) {
            return;
        }

        Message message = messageWrapper.get();
        if (!isMemberGroup(message.getSenderId(), message.getGroupId())) {
            return;
        }

        Emoji.Type emoji = Emoji.Type.valueOf(request.getEmojiId());
        Reaction newReaction = message.react(request.getSenderId(), emoji);
        messageRepository.save(message);

        pingGroup(message.getGroupId());

        User reactor = userRepository.findById(request.getSenderId()).orElse(null);
        ReactMessageResponse response = ReactMessageResponse.from(request, reactor);
        socketIOService.sendReact(response, message.getGroupId());
        notificationService.sendNewReactNotification(message, response, request.getSenderId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeReaction(String messageId, String senderId) {
        Optional<Message> messageWrapper = messageRepository.findById(messageId);
        if (!messageWrapper.isPresent()) {
            return;
        }

        Message message = messageWrapper.get();
        if (!isMemberGroup(senderId, message.getGroupId())) {
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Message saveMessage(Message data) {
        pingGroup(data.getGroupId());

        return messageRepository.save(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message saveTaskMessage(Task task) {
        var message = Message.builder()
                .senderId(task.getAssignerId())
                .groupId(task.getGroupId())
                .createdDate(task.getCreatedDate())
                .type(Message.Type.TASK)
                .taskId(task.getId())
                .build();

        return messageRepository.save(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message saveVoteMessage(Vote vote) {
        var message = Message.builder()
                .senderId(vote.getCreatorId())
                .groupId(vote.getGroupId())
                .createdDate(new Date())
                .type(Message.Type.VOTE)
                .voteId(vote.getId())
                .build();
        return messageRepository.save(message);
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public Message saveImageMessage(SendImagesRequest request) {
        List<String> imageKeys = new ArrayList<>();
        var tika = new Tika();

        for (MultipartFile file : request.getFiles()) {
            String key = blobStorage.generateBlobKey(tika.detect(file.getBytes()));
            blobStorage.post(file, key);
            imageKeys.add(key);
        }

        Message message = Message.builder()
                .id(request.getId())
                .senderId(request.getSenderId())
                .groupId(request.getGroupId())
                .createdDate(new Date())
                .type(Message.Type.IMAGE)
                .images(imageKeys)
                .build();

        pingGroup(request.getGroupId());
        return messageRepository.save(message);
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public Message saveFileMessage(SendFileRequest request) {
        var file = request.getFile();

        String key = blobStorage.generateBlobKey(new Tika().detect(file.getBytes()));
        blobStorage.post(file, key);

        FileModel fileModel = FileModel.builder()
                .id(key)
                .filename(request.getFile().getOriginalFilename())
                .size(request.getFile().getSize())
                .url(key)
                .build();
        Message message = Message.builder()
                .id(request.getId())
                .senderId(request.getSenderId())
                .groupId(request.getGroupId())
                .createdDate(new Date())
                .type(Message.Type.FILE)
                .file(fileModel)
                .build();
        pingGroup(request.getGroupId());
        return messageRepository.save(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessageDetailResponse> fulfillMessages(
            List<MessageResponse> messages,
            String viewerId) {
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageDetailResponse fulfillMeetingMessage(MessageResponse message) {
        Optional<Meeting> meeting = meetingRepository.findById(message.getMeetingId());
        return MessageDetailResponse.from(message, meeting.orElse(null));
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Reaction fulfillReaction(Reaction reaction, User reactor) {
        if (reactor == null) {
            return new Reaction();
        }
        reaction.update(reactor);
        return reaction;
    }

    private MessageDetailResponse fulfillReactions(
            MessageDetailResponse message,
            Map<String, User> reactors) {
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
        if(message.getType() == Message.Type.FORWARD)
            message.setType(Message.Type.TEXT);
        return switch (message.getType()) {
            case MEETING -> fulfillMeetingMessage(message);
            case TASK -> fulfillTaskMessage(message);
            case VOTE -> fulfillVotingMessage(message);
            case TEXT -> fulfillTextMessage(message);
            default -> MessageDetailResponse.from(message);
        };
    }

    private MessageDetailResponse fulfillVotingMessage(MessageResponse message) {
        Vote vote = voteRepository.findById(message.getVoteId()).orElse(null);
        if (vote != null) {
            vote.sortChoicesDesc();
        }
        return MessageDetailResponse.from(message, vote);
    }

    private Boolean isMemberGroup(String userId, String groupId) {
        var groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isPresent()) {
            var group = groupOpt.get();
            return group.getMentors().contains(userId) || group.getMentees().contains(userId);
        }

        var channelOpt = channelRepository.findById(groupId);
        if (channelOpt.isPresent()) {
            var channel = channelOpt.get();
            return channel.isMember(userId) || isMemberGroup(userId, channel.getParentId());
        }

        return false;
    }

    private void pingGroup(String groupId) {
        var groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isPresent()) {
            var group = groupOpt.get();
            group.ping();
            groupRepository.save(group);
        }

        var channelOpt = channelRepository.findById(groupId);
        if (channelOpt.isPresent()) {
            var channel = channelOpt.get();
            channel.ping();
            channelRepository.save(channel);
        }
    }


    /**
     * @param userId  String
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

package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.mapper.UserMapper;
import com.hcmus.mentor.backend.controller.payload.File;
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
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.constant.EmojiType;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import com.hcmus.mentor.backend.domain.dto.AssigneeDto;
import com.hcmus.mentor.backend.domain.dto.EmojiDto;
import com.hcmus.mentor.backend.domain.Reaction;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.service.MessageService;
import com.hcmus.mentor.backend.service.NotificationService;
import com.hcmus.mentor.backend.service.SocketIOService;
import com.hcmus.mentor.backend.service.fileupload.BlobStorage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@inheritDoc}
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final Logger logger = LogManager.getLogger(MessageServiceImpl.class);
    private final ChannelRepository channelRepository;
    private final GroupRepository groupRepository;
    private final MessageRepository messageRepository;
    private final MeetingRepository meetingRepository;
    private final TaskRepository taskRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final SocketIOService socketIOService;
    private final NotificationService notificationService;
    private final BlobStorage blobStorage;
    private final NotificationRepository notificationRepository;

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

        var sender = lastMessage.getSender();

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
        var message = messageRepository.findById(request.getMessageId()).orElse(null);
        if (message == null) {
            return;
        }
        User reactor = userRepository.findById(request.getSenderId()).orElse(null);
        if (reactor == null) {
            return;
        }

        Channel channel = message.getChannel();
        if (channel != null && !channel.isMember(request.getSenderId())) {
            return;
        }

        EmojiType emoji = EmojiType.valueOf(request.getEmojiId());
        message.react(reactor, emoji);

        messageRepository.save(message);

        ReactMessageResponse response = ReactMessageResponse.from(request, reactor);
        socketIOService.sendReact(response, channel.getId());
        notificationService.sendNewReactNotification(message, response, request.getSenderId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeReaction(String messageId, String senderId) {
        var message = messageRepository.findById(messageId).orElse(null);
        if (message == null) {
            return;
        }
        User reactor = userRepository.findById(senderId).orElse(null);
        if (reactor == null) {
            return;
        }

        Channel channel = message.getChannel();

        if (channel != null && !channel.isMember(senderId)) {
            return;
        }

        message.removeReact(reactor);
        Message updatedMessage = messageRepository.save(message);

        MessageDetailResponse.TotalReaction newTotalReaction = calculateTotalReactionMessage(updatedMessage);
        socketIOService.sendRemoveReact(new RemoveReactionResponse(messageId, senderId, newTotalReaction), channel.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageDetailResponse.TotalReaction calculateTotalReactionMessage(Message message) {
        List<EmojiDto> data = MessageDetailResponse.generateTotalReactionData(message.getReactions());
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
        pingGroup(data.getChannel().getId());

        return messageRepository.save(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message saveTaskMessage(Task task) {
        var message = Message.builder()
                .sender(task.getAssigner())
                .channel(task.getGroup())
                .createdDate(task.getCreatedDate())
                .type(Message.Type.TASK)
                .task(task)
                .build();

        return messageRepository.save(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message saveVoteMessage(Vote vote) {
        var message = Message.builder()
                .sender(vote.getCreator())
                .channel(vote.getGroup())
                .createdDate(new Date())
                .type(Message.Type.VOTE)
                .vote(vote)
                .build();
        return messageRepository.save(message);
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public Message saveImageMessage(SendImagesRequest request) {
        var user = userRepository.findById(request.getSenderId()).orElseThrow(() -> new DomainException("User not found"));
        var channel = channelRepository.findById(request.getGroupId()).orElseThrow(() -> new DomainException("Channel not found"));
        List<String> imageKeys = new ArrayList<>();
        var tika = new Tika();

        for (MultipartFile file : request.getFiles()) {
            String key = blobStorage.generateBlobKey(tika.detect(file.getBytes()));
            blobStorage.post(file, key);
            imageKeys.add(key);
        }

        Message message = Message.builder()
                .id(request.getId())
                .sender(user)
                .channel(channel)
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

        File fileModel = File.builder()
                .id(key)
                .filename(request.getFile().getOriginalFilename())
                .size(request.getFile().getSize())
                .url(key)
                .build();
        Message message = Message.builder()
                .id(request.getId())
                .sender(userRepository.findById(request.getSenderId()).orElseThrow(() -> new DomainException("User not found")))
                .channel(channelRepository.findById(request.getGroupId()).orElseThrow(() -> new DomainException("Channel not found")))
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
        var user = messages.stream()
                .flatMap(response -> response.getReactions().stream())
                .map(Reaction::getUser)
                .toList();
        Map<String, User> reactors = user.stream().collect(Collectors.toMap(User::getId, userDto -> userDto, (u1, u2) -> u2));
        return messages.stream()
                .map(this::fulfillMessage)
                .filter(Objects::nonNull)
                .filter(message -> !message.isDeletedAttach())
                .map(message -> fulfillReactions(message, reactors))
                .map(message -> MessageDetailResponse.totalReaction(message, viewerId))
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageDetailResponse fulfillTextMessage(MessageResponse message) {
        MessageDetailResponse response = MessageDetailResponse.from(message);
        if (message.getReply() != null) {
            Optional<Message> wrapper = messageRepository.findById(message.getReply());
            if (wrapper.isEmpty()) {
                return response;
            }
            // TODO: handle all function
            Message data = wrapper.get();
            String content = data.getContent();
            if (data.isDeleted()) {
                content = "Tin nhắn đã được xoá";
            }
            ShortProfile sender = userRepository.findShortProfile(data.getSender());
            var sennder  = UserMapper.INSTANCE.userToShortProfile(data.getSender());
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
        if (taskWrapper.isEmpty()) {
            return null;
        }
        TaskMessageResponse taskDetail = TaskMessageResponse.from(taskWrapper.get());

        List<TaskAssigneeResponse> assignees = getTaskAssignees(taskDetail.getId());
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
        List<Reaction> reactions = message.getReactions().stream()
                .map(reaction -> {
                    User reactor = reactors.getOrDefault(reaction.getUser(), null);
                    return fulfillReaction(reaction, reactor);
                })
                .filter(reaction -> reaction.getUser().getId() != null)
                .toList();
        message.setReactions(reactions);
        return message;
    }

    private MessageDetailResponse fulfillMessage(MessageResponse message) {
        return switch (message.getType()) {
            case MEETING -> fulfillMeetingMessage(message);
            case TASK -> fulfillTaskMessage(message);
            case VOTE -> fulfillVotingMessage(message);
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

//    private Boolean isMemberGroup(String userId, String groupId) {
//        var groupOpt = groupRepository.findById(groupId);
//
//        if (groupOpt.isPresent()) {
//            var group = groupOpt.get();
//            return group.isMember(userId);
//        }
//
//        var channelOpt = channelRepository.findById(groupId);
//        if (channelOpt.isPresent()) {
//            var channel = channelOpt.get();
//            return channel.isMember(userId) || isMemberGroup(userId, channel.getGroup().getId());
//        }
//
//        return false;
//    }

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

    private pingChannel(Channel channel) {
        channel.ping();
        channelRepository.save(channel);

    }


    /**
     * Only forward message type TEXT
     *
     * @param userId  String
     * @param request ForwardRequest
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Message> saveForwardMessage(String userId, ForwardRequest request) {
        List<String> typeAllow = List.of(Message.Type.TEXT.name(), Message.Type.IMAGE.name(), Message.Type.FILE.name(), Message.Type.VIDEO.name());
        var message = messageRepository.findById(request.getMessageId()).orElseThrow(() -> new DomainException("Message not found"));
        if (!typeAllow.contains(message.getType().name())) {
            throw new DomainException("Message type not allow forward");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new DomainException("User not found"));

        var channels = channelRepository.findByIdIn(request.getChannelIds());

//        groupRepository.findByIdIn(request.getChannelIds()).forEach(g -> channelIds.add(g.getId()));

        try {
            List<Message> messages = new ArrayList<>();
            channels.forEach(channel -> {
                Message m = Message.builder()
                        .sender(user)
                        .channel(channel)
                        .createdDate(new Date())
                        .content(message.getContent())
                        .type(message.getType())
                        .reply(message.getReply())
                        .images(message.getImages())
                        .file(message.getFile())
                        .isForward(true)
                        .build();

                messages.add(messageRepository.save(m));

            });
            messages.forEach(m -> {
                notificationService.sendForwardNotification(MessageDetailResponse.from(m, user), m.getGroupId());
                socketIOService.sendBroadcastMessage(MessageDetailResponse.from(m, user), m.getGroupId());
            });
            return messages;
        } catch (Exception e) {
            logger.log(Level.INFO, "Forward message failed", e);
            throw new DomainException("Forward message failed");
        }
    }

    @Override
    public boolean updateCreatedDateVoteMessage(String voteId) {
        var messageOpt = messageRepository.findByVoteId(voteId);
        if (messageOpt.isEmpty()) {
            return false;
        }
        var message = messageOpt.get();

        message.setCreatedDate(new Date());
        messageRepository.save(message);

        return true;
    }

    private List<TaskAssigneeResponse> getTaskAssignees(String taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return Collections.emptyList();
        }
        Task task = taskOpt.get();

        Optional<Group> groupOpt = groupRepository.findById(task.getGroupId());
        if (groupOpt.isEmpty()) {
            return Collections.emptyList();
        }
        Group group = groupOpt.get();

        List<String> assigneeIds = task.getAssignees().stream()
                .map(AssigneeDto::getUserId).toList();

        List<ProfileResponse> assignees = userRepository.findAllByIdIn(assigneeIds);

        Map<String, TaskStatus> statuses = task.getAssignees().stream()
                .collect(Collectors.toMap(AssigneeDto::getUserId, AssigneeDto::getStatus, (s1, s2) -> s2));

        return assignees.stream()
                .map(assignee -> {
                    TaskStatus status = statuses.getOrDefault(assignee.getId(), null);
                    boolean isMentor = group.isMentor(assignee.getId());
                    return TaskAssigneeResponse.from(assignee, status, isMentor);
                })
                .toList();
    }
}

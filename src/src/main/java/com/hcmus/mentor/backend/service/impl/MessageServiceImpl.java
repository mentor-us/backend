package com.hcmus.mentor.backend.service.impl;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.exception.DomainException;
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
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.controller.usecase.channel.updatelastmessage.UpdateLastMessageCommand;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.constant.EmojiType;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import com.hcmus.mentor.backend.domain.dto.EmojiDto;
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

import static com.hcmus.mentor.backend.domain.Message.Type.*;

/**
 * {@inheritDoc}
 */
@Service
@Transactional
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
    private final GroupUserRepository groupUserRepository;
    private final Pipeline pipeline;

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    @Transactional(readOnly = true)
    public Message find(String id) {
        var message = messageRepository.findById(id);

        return message.orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<MessageDetailResponse> getGroupMessages(
            String viewerId,
            String groupId,
            int page,
            int size) {
        List<MessageResponse> responses = messageRepository.getGroupMessagesByChannelId(groupId).stream()
                .map(message -> {
                    var sender = ProfileResponse.from(message.getSender());
                    return MessageResponse.from(message, sender);
                }).toList();

        return fulfillMessages(responses, viewerId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> findGroupMessagesByText(String groupId, String query, int page, int size) {
        return messageRepository.findGroupMessages(groupId, query).stream()
                .map(message -> {
                    var sender = ProfileResponse.from( message.getSender());
                    return MessageResponse.from(message, sender);
                }).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public String getLastGroupMessage(String groupId) {
        Message lastMessage = messageRepository.findTopByChannelIdOrderByCreatedDateDesc(groupId).orElse(null);

        return getMessageContent(lastMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public  String getMessageContentById(String messageId){
        var message = messageRepository.findById(messageId).orElse(null);

        return getMessageContent(message);
    }

    private String getMessageContent(Message message) {
        if (message == null) {
            return null;
        }
        if (Message.Status.DELETED.equals(message.getStatus())) {
            return "Tin nhắn đã được thu hồi.";
        }

        var sender = message.getSender();

        switch (message.getType()) {
            case TEXT:
                if (sender == null) {
                    return null;
                }
                String content = message.getContent();
                return sender.getName() + ": " + Jsoup.parse(content).text();
            case FILE:
                if (sender == null) {
                    return null;
                }
                return sender.getName() + " đã gửi tệp đính kèm mới.";
            case IMAGE:
                return message.getImages().size() + " ảnh mới.";
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
        return messageRepository.save(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message saveTaskMessage(Task task) {
        var message = messageRepository.save(Message.builder()
                .sender(task.getAssigner())
                .channel(task.getGroup())
                .createdDate(task.getCreatedDate())
                .type(Message.Type.TASK)
                .task(task)
                .build());
        pipeline.send(UpdateLastMessageCommand.builder().message(message).channel(task.getGroup()).build());
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message saveVoteMessage(Vote vote) {
        var message = messageRepository.save(Message.builder()
                .sender(vote.getCreator())
                .channel(vote.getGroup())
                .createdDate(new Date())
                .type(Message.Type.VOTE)
                .vote(vote)
                .build());
        pipeline.send(UpdateLastMessageCommand.builder().message(message).channel(message.getChannel()).build());
        return message;
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

        Message message = messageRepository.save( Message.builder()
                .id(request.getId())
                .sender(user)
                .channel(channel)
                .createdDate(new Date())
                .type(IMAGE)
                .images(imageKeys)
                .build());

        pingGroup(request.getGroupId());
        pipeline.send(UpdateLastMessageCommand.builder().message(message).channel(message.getChannel()).build());
        return message;
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
        Message message = messageRepository.save(Message.builder()
                .id(request.getId())
                .sender(userRepository.findById(request.getSenderId()).orElseThrow(() -> new DomainException("User not found")))
                .channel(channelRepository.findById(request.getGroupId()).orElseThrow(() -> new DomainException("Channel not found")))
                .createdDate(new Date())
                .type(FILE)
                .file(new File(fileModel))
                .build());
        pingGroup(request.getGroupId());
        pipeline.send(UpdateLastMessageCommand.builder().message(message).channel(message.getChannel()).build());
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessageDetailResponse> fulfillMessages(List<MessageResponse> messages, String viewerId) {
        List<User> users = messages.stream()
                .flatMap(response -> response.getReactions().stream())
                .map(Reaction::getUser)
                .toList();
        Map<String, User> reactors = users.stream().collect(Collectors.toMap(User::getId, user -> user, (u1, u2) -> u2));

        return messages.stream()
                .map(this::fulfillMessage)
                .filter(Objects::nonNull)
                .filter(message -> !message.isDeletedAttach())
                .map(this::processSpecialMessage)
                .map(message -> fulfillReactions(message, reactors))
                .map(message -> MessageDetailResponse.totalReaction(message, viewerId))
                .toList();
    }

    private MessageDetailResponse processSpecialMessage(MessageDetailResponse message) {
        if (Message.Status.DELETED.equals(message.getStatus())) {
            message.setContent("Tin nhắn đã được xoá");
        }

        if (message.getReply() != null) {
            var messageReply = messageRepository.findById(message.getReply().getId()).orElse(null);
            if (messageReply == null) {
                return message;
            }
            // Todo: vrify user is member of group
            var sender =messageReply.getSender();
            var senderProfile = sender.isStatus() ? new ShortProfile(sender) : null;
            message.setReply(MessageDetailResponse.ReplyMessage.builder()
                    .id(messageReply.getId())
                    .senderName(senderProfile != null ? sender.getName() : "Người dùng không tồn tại")
                    .content(messageReply.isDeleted() ? "Tin nhắn đã được xoá" : messageReply.getContent())
                    .build());
        }

        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageDetailResponse fulfillTextMessage(MessageResponse message) {
        MessageDetailResponse response = MessageDetailResponse.from(message);
        if (message.getReply() != null) {
            var messageReply = messageRepository.findById(message.getReply()).orElse(null);
            if (messageReply == null) {
                return response;
            }
            String content = messageReply.getContent();
            if (messageReply.isDeleted()) {
                content = "Tin nhắn đã được xoá";
            }
            ShortProfile sender = new ShortProfile(messageReply.getSender());
            String senderName = "Người dùng không tồn tại";
            if (groupUserRepository.existsByUserIdAndGroupId(sender.getId(), messageReply.getChannel().getGroup().getId())) {
                senderName = sender.getName();
            }
            MessageDetailResponse.ReplyMessage replyMessage =
                    MessageDetailResponse.ReplyMessage.builder()
                            .id(messageReply.getId())
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

        List<TaskAssigneeResponse> assignees = getTaskAssignees(message.getTaskId());
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

//    private pingChannel(Channel channel) {
//        channel.ping();
//        channelRepository.save(channel);
//
//    }


    /**
     * Only forward message type TEXT
     *
     * @param userId  String
     * @param request ForwardRequest
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Message> saveForwardMessage(String userId, ForwardRequest request) {
        List<String> typeAllow = List.of(TEXT.name(), IMAGE.name(), FILE.name(), Message.Type.VIDEO.name());
        User sender = userRepository.findById(userId).orElseThrow(() -> new DomainException("User not found"));
        Message message = messageRepository.findById(request.getMessageId()).orElseThrow(() -> new DomainException("Message not found"));
        if (!typeAllow.contains(message.getType().name()))
            throw new DomainException("Message type not allow forward");

        var channels = channelRepository.findByIdIn(request.getChannelIds());

        try {
            var messages = new ArrayList<Message>();
            channels.forEach(channel -> {
                Message m = messageRepository.save(Message.builder()
                        .sender(sender)
                        .channel(channel)
                        .createdDate(new Date())
                        .content(message.getContent())
                        .type(message.getType())
                        .reply(message.getReply())
                        .images(message.getImages())
                        .file(message.getFile())
                        .isForward(true)
                        .build());
                pipeline.send(UpdateLastMessageCommand.builder().message(message).channel(message.getChannel()).build());
                messages.add(m);
                pingGroup(channel.getGroup().getId());
            });
            messages.forEach(m -> {
                notificationService.sendForwardNotification(MessageDetailResponse.from(m, sender), m.getChannel().getGroup().getId());
                socketIOService.sendBroadcastMessage(MessageDetailResponse.from(m, sender), m.getChannel().getGroup().getId());
            });
            return messages;
        } catch (Exception e) {
            logger.log(Level.INFO, "Forward message failed", e);
            throw new DomainException("Forward message failed");
        }
    }

    @Override
    public boolean updateCreatedDateVoteMessage(String voteId) {
        var message = messageRepository.findByVoteId(voteId).orElse(null);
        if (message == null) {
            return false;
        }

        message.setCreatedDate(new Date());
        messageRepository.save(message);

        return true;
    }

    private List<TaskAssigneeResponse> getTaskAssignees(String taskId) {
       var task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return Collections.emptyList();
        }

        var group = task.getGroup().getGroup();

        List<ProfileResponse> assignees = task.getAssignees().stream()
                .map(Assignee::getUser)
                .map(ProfileResponse::from)
                .toList();

        Map<String, TaskStatus> statuses = task.getAssignees().stream()
                .collect(Collectors.toMap(a->a.getUser().getId(), Assignee::getStatus, (s1, s2) -> s2));

        return assignees.stream()
                .map(assignee -> {
                    boolean isMentor = group.isMentor(assignee.getId());
                    return TaskAssigneeResponse.from(assignee, statuses.get(assignee.getId()),isMentor);
                })
                .toList();
    }
}
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
import com.hcmus.mentor.backend.controller.usecase.channel.updatelastmessage.UpdateLastMessageCommand;
import com.hcmus.mentor.backend.controller.usecase.vote.common.VoteResult;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.constant.EmojiType;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import com.hcmus.mentor.backend.domain.dto.EmojiDto;
import com.hcmus.mentor.backend.domain.dto.ReactionDto;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.service.MessageService;
import com.hcmus.mentor.backend.service.NotificationService;
import com.hcmus.mentor.backend.service.SocketIOService;
import com.hcmus.mentor.backend.service.dto.MeetingDto;
import com.hcmus.mentor.backend.service.fileupload.BlobStorage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SocketIOService socketIOService;
    private final NotificationService notificationService;
    private final BlobStorage blobStorage;
    private final Pipeline pipeline;

    private final ModelMapper modelMapper;

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
    public List<MessageDetailResponse> getGroupMessages(String viewerId, String groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var messages = messageRepository.getGroupMessagesByChannelId(pageable, groupId).getContent();
        return mappingToMessageDetailResponse(messages, viewerId);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> findGroupMessagesByText(String groupId, String query, int page, int size) {
        return messageRepository.findGroupMessages(groupId, query).stream()
                .map(message -> mappingToMessageResponse(message, Optional.ofNullable(message.getSender()).map(User::getId).orElse(null)))
                .toList();
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
    public String getMessageContentById(String messageId) {
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
    @Transactional
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
//        notificationService.sendNewReactNotification(message, response, request.getSenderId());
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
        var reactionDtos = mappingReaction(message.getReactions());
        List<EmojiDto> data = MessageDetailResponse.generateTotalReactionData(reactionDtos);
        int total = MessageDetailResponse.calculateTotalReactionAmount(reactionDtos);

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
        var message = Message.builder()
                .sender(vote.getCreator())
                .channel(vote.getGroup())
                .createdDate(new Date())
                .type(Message.Type.VOTE)
                .vote(vote)
                .build();

        message = messageRepository.save(message);

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

        Message message = messageRepository.save(Message.builder()
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
     * Only forward message type TEXT
     *
     * @param userId  String
     * @param request ForwardRequest
     */
    @Override
    @Transactional()
    public void saveForwardMessage(String userId, ForwardRequest request) {

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

                pipeline.send(UpdateLastMessageCommand.builder()
                        .message(message)
                        .channel(message.getChannel())
                        .build());

                messages.add(m);
                pingGroup(channel.getGroup().getId());
            });
            messages.forEach(m -> {
                var response = mappingToMessageDetailResponse(m, sender.getId());
                notificationService.sendForwardNotification(response, m.getChannel().getGroup().getId());
                socketIOService.sendBroadcastMessage(response, m.getChannel().getGroup().getId());
            });
        } catch (Exception e) {
            logger.log(Level.INFO, "Forward message failed", e);
            throw new DomainException("Forward message failed");
        }
    }

    @Override
    public void updateCreatedDateVoteMessage(String voteId) {
        var message = messageRepository.findByVoteId(voteId).orElse(null);
        if (message == null) {
            return;
        }

        message.setCreatedDate(new Date());
        messageRepository.save(message);

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
                .collect(Collectors.toMap(a -> a.getUser().getId(), Assignee::getStatus, (s1, s2) -> s2));

        return assignees.stream()
                .map(assignee -> {
                    boolean isMentor = group.isMentor(assignee.getId());
                    return TaskAssigneeResponse.from(assignee, statuses.get(assignee.getId()), isMentor);
                })
                .toList();
    }

    @Override
    public List<MessageDetailResponse> mappingToMessageDetailResponse(List<Message> messages, String viewerId) {
        return messages.stream()
                .map(message -> mappingToMessageDetailResponse(message, viewerId))
                .toList();
    }

    @Override
    public MessageDetailResponse mappingToMessageDetailResponse(Message message, String viewerId) {
        var messageDetailResponse = modelMapper.map(message, MessageDetailResponse.class);
        switch (message.getType()) {
            case VOTE -> messageDetailResponse.setVote(modelMapper.map(message.getVote(), VoteResult.class));
            case TASK -> messageDetailResponse.setTask(modelMapper.map(message.getTask(), TaskMessageResponse.class));
            case MEETING -> messageDetailResponse.setMeeting(modelMapper.map(message.getMeeting(), MeetingDto.class));
            case FILE -> messageDetailResponse.setFile(modelMapper.map(message.getFile(), FileModel.class));
            case IMAGE ->
                    messageDetailResponse.setImages(message.getImages().stream().map(url -> MessageDetailResponse.Image.builder().url(url).build()).toList());
        }

        if (!List.of(VOTE, TASK, MEETING).contains(message.getType())) {
            var reactions = mappingReaction(message.getReactions());
            messageDetailResponse.setReactions(reactions);

            var reactionDtos = mappingEmojiFromDto(reactions);
            var ownerReaction = reactions.stream().filter(re -> Objects.equals(re.getUserId(), viewerId)).findFirst().orElse(null);
            messageDetailResponse.setTotalReaction(MessageDetailResponse.TotalReaction.builder()
                    .data(reactionDtos)
                    .ownerReacted(ownerReaction != null ? mappingEmojiFromDto(Collections.singletonList(ownerReaction)) : Collections.emptyList())
                    .total(sumEmojis(reactionDtos))
                    .build());
        }
        return messageDetailResponse;

    }

    @Override
    public MessageResponse mappingToMessageResponse(Message message, String viewerId) {
        var messageResponse = modelMapper.map(message, MessageResponse.class);

        switch (message.getType()) {
            case FILE -> messageResponse.setFile(modelMapper.map(message.getFile(), FileModel.class));
            case IMAGE -> messageResponse.setImages(message.getImages());
            case TASK ->
                    messageResponse.setTaskId(Optional.ofNullable(message.getTask()).map(Task::getId).orElse(null));
            case MEETING ->
                    messageResponse.setMeetingId(Optional.ofNullable(message.getMeeting()).map(Meeting::getId).orElse(null));
            case VOTE ->
                    messageResponse.setVoteId(Optional.ofNullable(message.getVote()).map(Vote::getId).orElse(null));
        }
        messageResponse.setReactions(mappingReaction(message.getReactions()));

        return messageResponse;
    }

    private List<ReactionDto> mappingReaction(List<Reaction> reactions) {
        return reactions.stream()
                .collect(Collectors.groupingBy(reaction -> reaction.getUser().getId()))
                .values()
                .stream()
                .map(reactionList -> {
                    User user = reactionList.getFirst().getUser();
                    ReactionDto reactionDto = ReactionDto.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .imageUrl(user.getImageUrl())
                            .build();

                    var emojiDtos = mappingEmoji(reactionList);
                    reactionDto.setData(emojiDtos);
                    reactionDto.setTotal(sumEmojis(emojiDtos));

                    return reactionDto;
                })
                .toList();
    }

    private List<EmojiDto> mappingEmoji(List<Reaction> reactions) {

        if (reactions == null || reactions.isEmpty())
            return Collections.emptyList();

        return reactions.stream()
                .collect(Collectors.groupingBy(Reaction::getEmojiType))
                .values()
                .stream()
                .map(reactions1 -> EmojiDto.builder()
                        .id(reactions1.stream().findFirst().get().getEmojiType())
                        .total(reactions1.stream().map(Reaction::getTotal).reduce(0, Integer::sum)).build())
                .toList();
    }

    private List<EmojiDto> mappingEmojiFromDto(List<ReactionDto> reactionDtos) {

        if (reactionDtos == null || reactionDtos.isEmpty())
            return Collections.emptyList();

        var reactions = reactionDtos.stream().flatMap(r -> r.getData().stream()).toList();

        return Arrays.stream(EmojiType.values()).map(
                emojiType -> EmojiDto.builder()
                        .id(emojiType)
                        .total(sumEmojis(reactions.stream().filter(m -> m.getId() == emojiType).toList()))
                        .build()
        ).toList();
    }

    private List<EmojiDto> generateEmojiDtos() {
        return Arrays.stream(EmojiType.values()).map(
                emojiType -> EmojiDto.builder().id(emojiType).total(0).build()
        ).toList();
    }

    private Integer sumEmojis(List<EmojiDto> emojis) {
        return emojis.stream().map(EmojiDto::getTotal).reduce(0, Integer::sum);
    }
}
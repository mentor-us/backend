package com.hcmus.mentor.backend.service.impl;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.FileModel;
import com.hcmus.mentor.backend.controller.payload.request.meetings.ForwardRequest;
import com.hcmus.mentor.backend.controller.payload.request.messages.ReactMessageRequest;
import com.hcmus.mentor.backend.controller.payload.request.messages.SendFileRequest;
import com.hcmus.mentor.backend.controller.payload.request.messages.SendImagesRequest;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.ReactMessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.RemoveReactionResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskMessageResponse;
import com.hcmus.mentor.backend.controller.usecase.channel.updatelastmessage.UpdateLastMessageCommand;
import com.hcmus.mentor.backend.controller.usecase.vote.common.VoteResult;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.constant.EmojiType;
import com.hcmus.mentor.backend.domain.dto.EmojiDto;
import com.hcmus.mentor.backend.domain.dto.ReactionDto;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.MessageRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.MessageService;
import com.hcmus.mentor.backend.service.NotificationService;
import com.hcmus.mentor.backend.service.SocketIOService;
import com.hcmus.mentor.backend.service.dto.MeetingDto;
import com.hcmus.mentor.backend.service.fileupload.BlobStorage;
import com.hcmus.mentor.backend.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SocketIOService socketIOService;
    private final NotificationService notificationService;
    private final BlobStorage blobStorage;
    private final Pipeline pipeline;
    private final ModelMapper modelMapper;

    private static final String MESSAGE_NOT_FOUND = "Message not found";
    private static final String USER_NOT_FOUND = "User not found";
    private static final List<String> ALLOWED_MESSAGE_TYPES = List.of(TEXT.name(), IMAGE.name(), FILE.name(), VIDEO.name());


    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    @Transactional(readOnly = true)
    public Message find(String id) {
        return messageRepository.findById(id).orElseThrow(() -> new DomainException(MESSAGE_NOT_FOUND));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<MessageDetailResponse> getGroupMessages(String viewerId, String groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        var messages = messageRepository.getGroupMessagesByChannelId(groupId, viewerId, pageable).getContent();

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
        Message message = messageRepository.findById(request.getMessageId()).orElseThrow(() -> new DomainException(MESSAGE_NOT_FOUND));
        User reactor = userRepository.findById(request.getSenderId()).orElseThrow(() -> new DomainException("Reactor not found"));
        Channel channel = message.getChannel();
        if (channel != null && !channel.isMember(request.getSenderId())) {
            throw new ForbiddenException("Forbidden");
        }

        EmojiType emoji = EmojiType.valueOf(request.getEmojiId());
        message.react(reactor, emoji);

        messageRepository.save(message);

        ReactMessageResponse response = ReactMessageResponse.from(request, reactor);
        socketIOService.sendReact(response, Objects.requireNonNull(channel).getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeReaction(String messageId, String senderId) {
        var message = messageRepository.findById(messageId).orElseThrow(() -> new DomainException(MESSAGE_NOT_FOUND));
        User reactor = userRepository.findById(senderId).orElseThrow(() -> new DomainException("Reactor not found"));
        Channel channel = message.getChannel();
        if (channel != null && !channel.isMember(senderId)) {
            throw new ForbiddenException("Forbidden");
        }

        message.getReactions().removeIf(reaction -> reaction.getUser().getId().equals(reactor.getId()));
        Message updatedMessage = messageRepository.saveAndFlush(message);

        MessageDetailResponse.TotalReaction newTotalReaction = calculateTotalReactionMessage(updatedMessage);
        socketIOService.sendRemoveReact(new RemoveReactionResponse(messageId, senderId, newTotalReaction), Objects.requireNonNull(channel).getId());
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
        var message = messageRepository.save(Message.builder()
                .sender(vote.getCreator())
                .channel(vote.getGroup())
                .createdDate(DateUtils.getDateNowAtUTC())
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
        var tika = new Tika();
        List<String> imageKeys = Arrays.stream(request.getFiles()).map(file -> {
            try {
                var key = blobStorage.generateBlobKey(tika.detect(file.getBytes()));
                blobStorage.post(file, key);

                return key;
            } catch (Exception e) {
                throw new DomainException("Save image message failed", e);
            }
        }).toList();
        Message message = messageRepository.save(Message.builder()
                .id(request.getId())
                .sender(userRepository.findById(request.getSenderId()).orElseThrow(() -> new DomainException(USER_NOT_FOUND)))
                .channel(channelRepository.findById(request.getGroupId()).orElseThrow(() -> new DomainException("Channel not found")))
                .createdDate(DateUtils.getDateNowAtUTC())
                .type(IMAGE)
                .images(imageKeys)
                .build());

        pipeline.send(UpdateLastMessageCommand.builder().message(message).channel(message.getChannel()).build());
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public Message saveFileMessage(SendFileRequest request) {

        var multipartFile = request.getFile();

        String key = blobStorage.generateBlobKey(new Tika().detect(multipartFile.getBytes()));
        blobStorage.post(multipartFile, key);

        File file = File.builder()
                .id(key)
                .filename(request.getFile().getOriginalFilename())
                .size(request.getFile().getSize())
                .url(key)
                .build();
        Message message = messageRepository.save(Message.builder()
                .id(request.getId())
                .sender(userRepository.findById(request.getSenderId()).orElseThrow(() -> new DomainException(USER_NOT_FOUND)))
                .channel(channelRepository.findById(request.getGroupId()).orElseThrow(() -> new DomainException("Channel not found")))
                .createdDate(DateUtils.getDateNowAtUTC())
                .type(FILE)
                .file(file)
                .build());

        pipeline.send(UpdateLastMessageCommand.builder().message(message).channel(message.getChannel()).build());
        return message;
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
        User sender = userRepository.findById(userId).orElseThrow(() -> new DomainException(USER_NOT_FOUND));
        Message oldMessage = messageRepository.findById(request.getMessageId()).orElseThrow(() -> new DomainException(MESSAGE_NOT_FOUND));
        if (oldMessage.isDeleted()) {
            throw new DomainException("Tin nhắn đã bị xóa");
        }
        if (!ALLOWED_MESSAGE_TYPES.contains(oldMessage.getType().name())) {
            throw new DomainException("Không thể chuyển tiếp tin nhắn loại này");
        }
        var channels = channelRepository.findByIdIn(request.getChannelIds());

        try {
            var messages = channels.stream().map(
                    channel -> {
                        Message message = Message.builder()
                                .sender(sender)
                                .channel(channel)
                                .createdDate(DateUtils.getDateNowAtUTC())
                                .content(oldMessage.getContent())
                                .type(oldMessage.getType())
                                .isForward(true)
                                .build();

                        if (oldMessage.getType() == IMAGE) {
                            var copiedImages = oldMessage.getImages().stream().map(blobStorage::copyFile).toList();
                            message.setImages(copiedImages);
                        } else if (oldMessage.getType() == FILE) {
                            var newFile = File.builder()
                                    .filename(oldMessage.getFile().getFilename())
                                    .size(oldMessage.getFile().getSize())
                                    .url(blobStorage.copyFile(oldMessage.getFile().getUrl()))
                                    .build();
                            message.setFile(newFile);
                        }
                        messageRepository.save(message);

                        pipeline.send(UpdateLastMessageCommand.builder()
                                .message(message)
                                .channel(channel)
                                .build());

                        var response = mappingToMessageDetailResponse(message, sender.getId());
                        socketIOService.sendBroadcastMessage(response, message.getChannel().getId());
                        return message;
                    }
            ).toList();
            notificationService.sendForForwardMessage(messages, sender);
        } catch (Exception e) {
            throw new DomainException("Forward message failed", e);
        }
    }


    @Override
    public void updateCreatedDateVoteMessage(String voteId) {
        messageRepository.findByVoteId(voteId).ifPresent(message -> {
            message.setCreatedDate(DateUtils.getDateNowAtUTC());
            messageRepository.save(message);
        });
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

        Optional.ofNullable(message.getReply()).flatMap(messageRepository::findById).ifPresent(replyMessage -> messageDetailResponse.setReply(MessageDetailResponse.ReplyMessage.builder()
                .id(replyMessage.getId())
                .content(replyMessage.getStatus() ==
                        Message.Status.DELETED
                        ? "Tin nhắn đã được xóa"
                        : switch (replyMessage.getType()) {
                    case TEXT -> replyMessage.getContent();
                    case FILE -> "Tệp đính kèm";
                    case IMAGE -> "Ảnh đính kèm";
                    case VIDEO -> "Video đính kèm";
                    case MEETING -> "Lịch hẹn đính kèm";
                    case TASK -> "Công việc đính kèm";
                    case VOTE -> "Cuộc bình chọn đính kèm";
                    case NOTIFICATION -> "Thông báo đính kèm";
                    case SYSTEM -> "";
                })
                .senderName(replyMessage.getSender().getName())
                .build()));

        switch (message.getType()) {
            case VOTE -> messageDetailResponse.setVote(modelMapper.map(message.getVote(), VoteResult.class));
            case TASK -> messageDetailResponse.setTask(modelMapper.map(message.getTask(), TaskMessageResponse.class));
            case MEETING -> messageDetailResponse.setMeeting(modelMapper.map(message.getMeeting(), MeetingDto.class));
            case FILE -> messageDetailResponse.setFile(modelMapper.map(message.getFile(), FileModel.class));
            case IMAGE ->
                    messageDetailResponse.setImages(message.getImages().stream().map(url -> MessageDetailResponse.Image.builder().url(url).build()).toList());
            default -> {
            }
        }

        if (!List.of(VOTE, TASK, MEETING).contains(message.getType())) {
            var reactions = mappingReaction(message.getReactions());
            messageDetailResponse.setReactions(reactions);

            var reactionDtos = mappingEmojiFromDto(reactions);
            var ownerReaction = reactions.stream().filter(re -> Objects.equals(re.getUserId(), viewerId)).findFirst().orElse(null);
            messageDetailResponse.setTotalReaction(MessageDetailResponse.TotalReaction.builder()
                    .data(reactionDtos)
                    .ownerReacted(Optional.ofNullable(ownerReaction)
                            .map(ownerReaction1 -> mappingEmojiFromDto(Collections.singletonList(ownerReaction1)))
                            .orElse(Collections.emptyList()))
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
            default -> {
            }
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
        return Optional.ofNullable(reactionDtos).map(reactionDtos1 -> {
            var reactions = reactionDtos1.stream().flatMap(r -> r.getData().stream()).toList();
            return reactions.stream()
                    .collect(Collectors.groupingBy(EmojiDto::getId))
                    .entrySet()
                    .stream()
                    .map(entry -> EmojiDto.builder()
                            .id(entry.getKey())
                            .total(entry.getValue().stream().map(EmojiDto::getTotal).reduce(0, Integer::sum)).build())
                    .toList();
        }).orElse(Collections.emptyList());
    }

    private Integer sumEmojis(List<EmojiDto> emojis) {
        return emojis.stream().map(EmojiDto::getTotal).reduce(0, Integer::sum);
    }
}
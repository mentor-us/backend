package com.hcmus.mentor.backend.service;

import com.google.api.services.drive.model.File;
import com.hcmus.mentor.backend.entity.*;
import com.hcmus.mentor.backend.manager.GoogleDriveManager;
import com.hcmus.mentor.backend.payload.FileModel;
import com.hcmus.mentor.backend.payload.request.ReactMessageRequest;
import com.hcmus.mentor.backend.payload.response.messages.RemoveReactionResponse;
import com.hcmus.mentor.backend.payload.request.SendFileRequest;
import com.hcmus.mentor.backend.payload.request.SendImagesRequest;
import com.hcmus.mentor.backend.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.payload.response.messages.ReactMessageResponse;
import com.hcmus.mentor.backend.payload.response.tasks.TaskAssigneeResponse;
import com.hcmus.mentor.backend.payload.response.tasks.TaskMessageResponse;
import com.hcmus.mentor.backend.repository.*;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final GroupService groupService;

    private final MessageRepository messageRepository;

    private final GoogleDriveManager googleDriveManager;

    private final MeetingRepository meetingRepository;

    private final TaskRepository taskRepository;

    private final VoteRepository voteRepository;

    private final SocketIOService socketIOService;

    private final UserRepository userRepository;

    private final MongoTemplate mongoTemplate;

    private final NotificationService notificationService;

    private final GroupRepository groupRepository;

    private final VoteService voteService;

    private final TaskService taskService;

    @Value("${app.googleDrive.rootId}")
    private String rootId;

    public MessageService(@Lazy GroupService groupService,
                          MessageRepository messageRepository,
                          GoogleDriveManager googleDriveManager, MeetingRepository meetingRepository, TaskRepository taskRepository, VoteRepository voteRepository, SocketIOService socketIOService, UserRepository userRepository, MongoTemplate mongoTemplate, NotificationService notificationService, GroupRepository groupRepository, VoteService voteService, TaskService taskService) {
        this.groupService = groupService;
        this.messageRepository = messageRepository;
        this.googleDriveManager = googleDriveManager;
        this.meetingRepository = meetingRepository;
        this.taskRepository = taskRepository;
        this.voteRepository = voteRepository;
        this.socketIOService = socketIOService;
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
        this.notificationService = notificationService;
        this.groupRepository = groupRepository;
        this.voteService = voteService;
        this.taskService = taskService;
    }

    public List<MessageDetailResponse> getGroupMessages(String viewerId, String groupId, int page, int size) {
        List<MessageResponse> responses = messageRepository
                .getGroupMessagesByGroupId(groupId, page * size, size);
        return fulfillMessages(responses, viewerId);
    }

    public List<MessageDetailResponse> fulfillMessages(List<MessageResponse> messages, String viewerId) {
        List<String> userIds = messages.stream()
                .flatMap(response -> response.getReactions().stream())
                .map(Reaction::getUserId)
                .collect(Collectors.toList());
        Map<String, User> reactors = userRepository.findByIdIn(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user, (u1, u2) -> u2));
        return messages.stream()
                .map(this::fulfillMessage)
                .filter(Objects::nonNull)
                .filter(message -> !message.isDeletedAttach())
                .map(message -> fulfillReactions(message, reactors))
                .map(message -> MessageDetailResponse.totalReaction(message, viewerId))
                .collect(Collectors.toList());
    }

    private MessageDetailResponse fulfillReactions(MessageDetailResponse message, Map<String, User> reactors) {
        List<Reaction> reactions = message.getReactions().stream()
                .map(reaction -> {
                    User reactor = reactors.getOrDefault(reaction.getUserId(), null);
                    return fulfillReaction(reaction, reactor);
                })
                .filter(reaction -> reaction.getUserId() != null)
                .collect(Collectors.toList());
        message.setReactions(reactions);
        return message;
    }

    public Reaction fulfillReaction(Reaction reaction, User reactor) {
        if (reactor == null) {
            return new Reaction();
        }
        reaction.update(reactor);
        return reaction;
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
            default:
                response = MessageDetailResponse.from(message);
        }
        return response;
    }

    private MessageDetailResponse fulfillVotingMessage(MessageResponse message) {
        Vote vote = voteRepository.findById(message.getVoteId())
                .orElse(null);
        if (vote != null) {
            vote.sortChoicesDesc();
        }
        return MessageDetailResponse.from(message, vote);
    }

    public MessageDetailResponse fulfillMeetingMessage(MessageResponse message) {
        Optional<Meeting> meeting = meetingRepository.findById(message.getMeetingId());
        return MessageDetailResponse.from(message, meeting.orElse(null));
    }

    public MessageDetailResponse fulfillTaskMessage(MessageResponse message) {
        Optional<Task> taskWrapper = taskRepository.findById(message.getTaskId());
        if (!taskWrapper.isPresent()) {
            return null;
        }
        TaskMessageResponse taskDetail = TaskMessageResponse.from(taskWrapper.get());

        List<TaskAssigneeResponse> assignees = taskService
                .getTaskAssignees(taskDetail.getId());
        taskDetail.setAssignees(assignees);
        return MessageDetailResponse.from(message, taskDetail);
    }

    public List<MessageResponse> findGroupMessagesByText(String groupId, String query,
                                                         int page, int size) {
        List<MessageResponse> responses = messageRepository
                .findGroupMessages(groupId, query, page * size, size);
        return responses;
    }

    public Message saveMessage(Message data) {
        groupService.pingGroup(data.getGroupId());

        return messageRepository.save(data);
    }

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

        User reactor = userRepository.findById(request.getSenderId())
                        .orElse(null);
        ReactMessageResponse response = ReactMessageResponse.from(request, reactor);
        socketIOService.sendReact(response, message.getGroupId());
        notificationService.sendNewReactNotification(message, response, request.getSenderId());
    }

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

        MessageDetailResponse.TotalReaction newTotalReaction = calculateTotalReactionMessage(updatedMessage);
        socketIOService.sendRemoveReact(new RemoveReactionResponse(messageId, senderId, newTotalReaction), updatedMessage.getGroupId());
    }

    public MessageDetailResponse.TotalReaction calculateTotalReactionMessage(Message message) {
        List<Emoji> data = MessageDetailResponse.generateTotalReactionData(message.getReactions());
        int total = MessageDetailResponse.calculateTotalReactionAmount(message.getReactions());
        return MessageDetailResponse.TotalReaction.builder()
                .data(data)
                .ownerReacted(Collections.emptyList())
                .total(total)
                .build();
    }

    public Message saveImageMessage(SendImagesRequest request) throws GeneralSecurityException, IOException {
        List<String> images = new ArrayList<>();
        for (MultipartFile file : request.getFiles()) {
            File uploadedFile = googleDriveManager.uploadToFolder(request.getGroupId(), file);
//            images.add("https://lh3.google.com/u/0/d/" + uploadedFile.getId());
            images.add("https://drive.google.com/uc?export=view&id=" + uploadedFile.getId());
        }
        Message message = Message.builder()
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

    public Message saveFileMessage(SendFileRequest request) throws GeneralSecurityException, IOException {
        File uploadedFile = googleDriveManager.uploadToFolder(request.getGroupId(), request.getFile());
        String url = "https://drive.google.com/uc?export=download&id=" + uploadedFile.getId();

        FileModel file = FileModel.builder()
                .id(uploadedFile.getId())
                .filename(request.getFile().getOriginalFilename())
                .size(request.getFile().getSize())
                .url(url)
                .build();
        Message message = Message.builder()
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

    public Message saveTaskMessage(Task task) {
        Message message = Message.builder()
                .senderId(task.getAssignerId())
                .groupId(task.getGroupId())
                .createdDate(task.getCreatedDate())
                .type(Message.Type.TASK)
                .taskId(task.getId())
                .build();

        return messageRepository.save(message);
    }

    public String getLastGroupMessage(String groupId) {
        Message lastMessage = messageRepository
                .findTopByGroupIdOrderByCreatedDateDesc(groupId).orElse(null);
        if (lastMessage == null) {
            return null;
        }
        if (Message.Status.DELETED.equals(lastMessage.getStatus())) {
            return "Tin nhắn đã được thu hồi.";
        }
        User sender = userRepository
                .findById(lastMessage.getSenderId()).orElse(null);
        switch (lastMessage.getType()) {
            case TEXT:
                if (sender == null) {
                    return "";
                }
                return sender.getName() + ": " + Jsoup.parse(lastMessage.getContent()).text();
            case FILE:
                if (sender == null) {
                    return "";
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

    public Message saveVoteMessage(Vote vote) {
        Message message = Message.builder()
                .senderId(vote.getCreatorId())
                .groupId(vote.getGroupId())
                .createdDate(new Date())
                .type(Message.Type.VOTE)
                .voteId(vote.getId())
                .build();
        return messageRepository.save(message);
    }
}

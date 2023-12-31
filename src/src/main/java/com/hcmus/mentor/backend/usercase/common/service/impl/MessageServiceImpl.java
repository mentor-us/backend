package com.hcmus.mentor.backend.usercase.common.service.impl;

import com.hcmus.mentor.backend.entity.*;
import com.hcmus.mentor.backend.infrastructure.fileupload.BlobStorage;
import com.hcmus.mentor.backend.payload.FileModel;
import com.hcmus.mentor.backend.payload.request.ReactMessageRequest;
import com.hcmus.mentor.backend.payload.request.SendFileRequest;
import com.hcmus.mentor.backend.payload.request.SendImagesRequest;
import com.hcmus.mentor.backend.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.payload.response.messages.ReactMessageResponse;
import com.hcmus.mentor.backend.payload.response.messages.RemoveReactionResponse;
import com.hcmus.mentor.backend.payload.response.tasks.TaskAssigneeResponse;
import com.hcmus.mentor.backend.payload.response.tasks.TaskMessageResponse;
import com.hcmus.mentor.backend.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.usercase.common.repository.MeetingRepository;
import com.hcmus.mentor.backend.usercase.common.repository.MessageRepository;
import com.hcmus.mentor.backend.usercase.common.repository.TaskRepository;
import com.hcmus.mentor.backend.usercase.common.repository.UserRepository;
import com.hcmus.mentor.backend.usercase.common.repository.VoteRepository;
import com.hcmus.mentor.backend.usercase.common.service.GroupService;
import com.hcmus.mentor.backend.usercase.common.service.MessageService;
import com.hcmus.mentor.backend.usercase.common.service.NotificationService;
import com.hcmus.mentor.backend.usercase.common.service.SocketIOService;
import com.hcmus.mentor.backend.usercase.common.service.TaskServiceImpl;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
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
  public Message saveImageMessage(SendImagesRequest request) throws IOException {
    List<String> images = new ArrayList<>();
    for (MultipartFile multipartFile : request.getFiles()) {
      File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
      try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
        fileOutputStream.write(multipartFile.getBytes());
      }
      String mimeType = new Tika().detect(file);

      String key = blobStorage.generateBlobKey(mimeType);

      blobStorage.post(key, file);
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
  public Message saveFileMessage(SendFileRequest request) throws IOException {
    var multipartFile = request.getFile();

    File tempfile = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
    try (FileOutputStream fileOutputStream = new FileOutputStream(tempfile)) {
      fileOutputStream.write(multipartFile.getBytes());
    }
    String mimeType = new Tika().detect(tempfile);

    String key = blobStorage.generateBlobKey(mimeType);

    blobStorage.post(key, tempfile);

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
}

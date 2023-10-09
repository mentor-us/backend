package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.entity.*;
import com.hcmus.mentor.backend.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.payload.response.tasks.TaskAssigneeResponse;
import com.hcmus.mentor.backend.payload.response.tasks.TaskMessageResponse;
import com.hcmus.mentor.backend.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.service.MessageService2;
import com.hcmus.mentor.backend.service.TaskServiceImpl;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService2Impl implements MessageService2 {

  private final UserRepository userRepository;
  private final MessageRepository messageRepository;
  private final VoteRepository voteRepository;
  private final MeetingRepository meetingRepository;
  private final TaskRepository taskRepository;
  private final TaskServiceImpl taskService;

  @Override
  public List<MessageDetailResponse> fulfillMessages(
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

  private Reaction fulfillReaction(Reaction reaction, User reactor) {
    if (reactor == null) {
      return new Reaction();
    }
    reaction.update(reactor);
    return reaction;
  }

  private MessageDetailResponse fulfillTextMessage(MessageResponse message) {
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

  private MessageDetailResponse fulfillTaskMessage(MessageResponse message) {
    Optional<Task> taskWrapper = taskRepository.findById(message.getTaskId());
    if (!taskWrapper.isPresent()) {
      return null;
    }
    TaskMessageResponse taskDetail = TaskMessageResponse.from(taskWrapper.get());

    List<TaskAssigneeResponse> assignees = taskService.getTaskAssignees(taskDetail.getId());
    taskDetail.setAssignees(assignees);
    return MessageDetailResponse.from(message, taskDetail);
  }

  private MessageDetailResponse fulfillMeetingMessage(MessageResponse message) {
    Optional<Meeting> meeting = meetingRepository.findById(message.getMeetingId());
    return MessageDetailResponse.from(message, meeting.orElse(null));
  }

  private MessageDetailResponse fulfillVotingMessage(MessageResponse message) {
    Vote vote = voteRepository.findById(message.getVoteId()).orElse(null);
    if (vote != null) {
      vote.sortChoicesDesc();
    }
    return MessageDetailResponse.from(message, vote);
  }

  @Override
  public String getLastGroupMessage(String groupId) {
    Message lastMessage =
        messageRepository.findTopByGroupIdOrderByCreatedDateDesc(groupId).orElse(null);
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
}

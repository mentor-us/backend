package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.entity.*;
import com.hcmus.mentor.backend.payload.request.AddNotificationRequest;
import com.hcmus.mentor.backend.payload.request.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.payload.request.SubscribeNotificationRequest;
import com.hcmus.mentor.backend.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.payload.response.messages.ReactMessageResponse;
import com.hcmus.mentor.backend.payload.response.tasks.TaskMessageResponse;
import java.util.Map;
import org.springframework.scheduling.annotation.Async;

public interface NotificationService {
  Map<String, Object> getOwnNotifications(String userId, int page, int size);

  Notif createResponseNotification(String senderId, AddNotificationRequest request);

  Notif responseNotification(String userId, String notificationId, String action);

  void subscribeNotification(SubscribeNotificationRequest request);

  void unsubscribeNotification(String userId);

  @Async
  void sendNewMessageNotification(MessageDetailResponse message);

  @Async
  void sendNewTaskNotification(MessageDetailResponse message);

  Notif createNewTaskNotification(
      String title, String content, String senderId, TaskMessageResponse task);

  @Async
  void sendNewMeetingNotification(MessageDetailResponse message);

  Notif createNewMeetingNotification(
      String title, String content, String senderId, Meeting meeting);

  @Async
  void sendNewMediaMessageNotification(MessageDetailResponse message);

  Notif createNewMediaNotification(String title, String content, String senderId, Group group);

  @Async
  void sendNewReactNotification(Message message, ReactMessageResponse reaction, String senderId);

  @Async
  void sendRescheduleMeetingNotification(
      String modifierId, Meeting meeting, RescheduleMeetingRequest request);

  Notif createRescheduleMeetingNotification(
      String title, String content, String senderId, Group group, Meeting meeting);

  long getUnreadNumber(String userId);

  @Async
  void sendNewVoteNotification(String creatorId, Vote vote);

  Notif createNewVoteNotification(
      String title, String content, String senderId, Group group, Vote vote);

  void sendNewPinNotification(MessageDetailResponse message, User pinner);

  void sendNewUnpinNotification(MessageDetailResponse message, User pinner);
}

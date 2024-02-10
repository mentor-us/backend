package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.controller.payload.request.AddNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.request.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.SubscribeNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.ReactMessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskMessageResponse;

import java.util.Map;

import org.springframework.scheduling.annotation.Async;

public interface NotificationService {
    Map<String, Object> getOwnNotifications(String userId, int page, int size);

    Notification createResponseNotification(String senderId, AddNotificationRequest request);

    Notification responseNotification(String userId, String notificationId, String action);

    void subscribeNotification(SubscribeNotificationRequest request);

    void unsubscribeNotification(String userId);

    @Async
    void sendNewMessageNotification(MessageDetailResponse message);

    @Async
    void sendNewTaskNotification(MessageDetailResponse message);

    Notification createNewTaskNotification(
            String title, String content, String senderId, TaskMessageResponse task);

    @Async
    void sendNewMeetingNotification(MessageDetailResponse message);

    Notification createNewMeetingNotification(
            String title, String content, String senderId, Meeting meeting);

    @Async
    void sendNewMediaMessageNotification(MessageDetailResponse message);

    Notification createNewMediaNotification(String title, String content, String senderId, Group group);

    @Async
    void sendNewReactNotification(Message message, ReactMessageResponse reaction, String senderId);

    @Async
    void sendRescheduleMeetingNotification(
            String modifierId, Meeting meeting, RescheduleMeetingRequest request);

    Notification createRescheduleMeetingNotification(
            String title, String content, String senderId, Group group, Meeting meeting);

    long getUnreadNumber(String userId);

    @Async
    void sendNewVoteNotification(String creatorId, Vote vote);

    Notification createNewVoteNotification(
            String title, String content, String senderId, Group group, Vote vote);

    void sendNewPinNotification(MessageDetailResponse message, User pinner);

    void sendNewUnpinNotification(MessageDetailResponse message, User pinner);
}

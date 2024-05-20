package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.payload.request.AddNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.request.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.SubscribeNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.response.messages.ReactMessageResponse;
import com.hcmus.mentor.backend.domain.*;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    Map<String, Object> getOwnNotifications(String userId, int page, int size);

    Notification createResponseNotification(String senderId, AddNotificationRequest request);

    Notification responseNotification(String userId, String notificationId, String action);

    void subscribeNotification(SubscribeNotificationRequest request);

    void unsubscribeNotification(String userId);

//    @Async
    void sendNewMessageNotification(Message message);

    void sendNewTaskNotification(Task task);

    void sendNewMeetingNotification(Meeting meeting);

//    @Async
    void sendNewMediaMessageNotification(Message message);

//    @Async
    void sendNewReactNotification(Message message, ReactMessageResponse reaction, User sender);

//    @Async
    void sendRescheduleMeetingNotification(
            User modifier, Meeting meeting, RescheduleMeetingRequest request);

    long getUnreadNumber(String userId);

    void sendNewVoteNotification(String creatorId, Vote vote);

//    @Async
    void sendTogglePinNotification(Message message, User pinner, Boolean isPin);

    void sendForwardMessageNotification(List<Message> messages, User sender);
}
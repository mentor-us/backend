package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.payload.request.meetings.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.notifications.AddNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.request.notifications.SubscribeNotificationServerRequest;
import com.hcmus.mentor.backend.controller.usecase.notification.common.NotificationDetailDto;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.constant.NotificationAction;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    NotificationDetailDto getById(String notificationId);

    Map<String, Object> getOwn(int page, int size);

    Notification create(AddNotificationRequest request);

    Notification response(String notificationId, NotificationAction action);

    void subscribeToServer(SubscribeNotificationServerRequest request);

    void unsubscribeNotification(String userId);

    long getCountUnread();

    void sendForMessage(Message message);

    void sendForTask(Task task);

    void sendForMeeting(Meeting meeting);

    void sendForMediaMessage(Message message);

    void sendForRescheduleMeeting(User modifier, Meeting meeting, RescheduleMeetingRequest request);

    void sendForNote(String creatorId, Vote vote);

    void sendForTogglePin(Message message, User pinner, boolean isPin);

    void sendForForwardMessage(List<Message> messages, User sender);
}
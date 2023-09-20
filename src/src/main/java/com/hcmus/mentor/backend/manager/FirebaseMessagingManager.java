package com.hcmus.mentor.backend.manager;

import com.google.firebase.messaging.*;
import com.hcmus.mentor.backend.entity.NotificationSubscriber;
import com.hcmus.mentor.backend.repository.NotificationSubscriberRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FirebaseMessagingManager {

    private final FirebaseMessaging firebaseMessaging;

    private final NotificationSubscriberRepository notificationSubscriberRepository;

    public FirebaseMessagingManager(FirebaseMessaging firebaseMessaging,
                                    NotificationSubscriberRepository notificationSubscriberRepository) {
        this.firebaseMessaging = firebaseMessaging;
        this.notificationSubscriberRepository = notificationSubscriberRepository;
    }

    public String sendNotification(String token, Notification notification, Map<String, String> data) throws FirebaseMessagingException {
        Message message = Message
                .builder()
                .setToken(token)
                .setNotification(notification)
                .putAllData(data)
                .build();

        return firebaseMessaging.send(message);
    }

    public String sendNotification(String token, Notification notification) throws FirebaseMessagingException {
        return sendNotification(token, notification, Collections.emptyMap());
    }

    public BatchResponse sendGroupNotification(List<String> receiverIds, String title, String content,
                                               Map<String, String> data) throws FirebaseMessagingException {
        List<String> tokens = getDeviceTokensFromUserIds(receiverIds);
        if (tokens == null || tokens.size() == 0) {
            return null;
        }
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(content)
                .build();
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(notification)
                .addAllTokens(tokens)
                .putAllData(data)
                .build();
        return firebaseMessaging.sendMulticast(message);
    }

    private List<String> getDeviceTokensFromUserIds(List<String> userIds) {
        return notificationSubscriberRepository.findAllByUserIdIn(userIds)
                .stream()
                .map(NotificationSubscriber::getToken)
                .distinct()
                .collect(Collectors.toList());
    }

    public BatchResponse sendGroupNotification(List<String> tokens, String title, String content) throws FirebaseMessagingException {
        return sendGroupNotification(tokens, title, content, Collections.emptyMap());
    }
}

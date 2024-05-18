package com.hcmus.mentor.backend.event;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FirebaseEventListener {

    private final Logger logger = LogManager.getLogger(FirebaseEventListener.class);

    private final FirebaseMessaging firebaseMessaging;

    @Async
    @EventListener
    public void sendFirebaseNotificationEventListener(SendFirebaseNotificationEvent event) throws FirebaseMessagingException {
        var tokens = event.getTokens();

        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        if (tokens.size() == 1) {
            var id = sendNotification(tokens.getFirst(), event.getTitle(), event.getBody(), event.getData());

            logger.debug("Sent notification with id: {}", id);
        } else {
            var results = sendMulticastNotification(tokens, event.getTitle(), event.getBody(), event.getData());

            logger.debug("Sent multicast notification with {} success, {} failure, response {}", results.getSuccessCount(), results.getFailureCount(), results.getResponses());
        }
    }

    private String sendNotification(String token, String title, String body, Map<String, String> data) throws FirebaseMessagingException {
        var notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .putAllData(data)
                .build();

        return firebaseMessaging.send(message);
    }

    private BatchResponse sendMulticastNotification(List<String> tokens, String title, String body, Map<String, String> data) throws FirebaseMessagingException {
        var notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        var message = MulticastMessage.builder()
                .setNotification(notification)
                .putAllData(data)
                .addAllTokens(tokens)
                .build();

        return firebaseMessaging.sendEachForMulticast(message);
    }
}

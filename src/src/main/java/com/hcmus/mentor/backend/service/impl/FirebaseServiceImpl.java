package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.domain.NotificationSubscriber;
import com.hcmus.mentor.backend.event.SendFirebaseNotificationEvent;
import com.hcmus.mentor.backend.repository.NotificationSubscriberRepository;
import com.hcmus.mentor.backend.service.FirebaseService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FirebaseServiceImpl implements FirebaseService {

    private static final Logger logger = LogManager.getLogger(FirebaseServiceImpl.class);
    private final NotificationSubscriberRepository notificationSubscriberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void sendNotificationMulticast(List<String> receiverIds, String title, String body, Map<String, String> data) {

        List<String> tokens = notificationSubscriberRepository.findAllByUserIdIn(receiverIds).stream()
                .map(NotificationSubscriber::getToken)
                .distinct()
                .toList();

        logger.info("Send multicast notification to receiverIds {}, token {}", receiverIds, tokens);
        logger.debug("Title: {}, body: {}, data: {}", title, body, data);

        var event = new SendFirebaseNotificationEvent(this, tokens, title, body, data);

        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void sendNotification(String receiverId, String title, String body, Map<String, String> data) {
        var subscriber = notificationSubscriberRepository.findByUserId(receiverId).orElse(null);
        if (subscriber == null) {
            logger.warn("Not found subscriber with id {}", receiverId);

            return;
        }

        var token = subscriber.getToken();

        logger.info("Send notification to receiverIds {}, token {}", receiverId, token);
        logger.debug("Title: {}, body: {}, data: {}", title, body, data);

        var event = new SendFirebaseNotificationEvent(this, List.of(token), title, body, data);

        applicationEventPublisher.publishEvent(event);
    }
}
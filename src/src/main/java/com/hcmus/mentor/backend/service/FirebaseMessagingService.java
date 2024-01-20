package com.hcmus.mentor.backend.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Notification;

import java.util.List;
import java.util.Map;

public interface FirebaseMessagingService {
    String sendNotification(String token, Notification notification, Map<String, String> data)
            throws FirebaseMessagingException;

    String sendNotification(String token, Notification notification)
            throws FirebaseMessagingException;

    BatchResponse sendGroupNotification(
            List<String> receiverIds, String title, String content, Map<String, String> data)
            throws FirebaseMessagingException;

    BatchResponse sendGroupNotification(List<String> tokens, String title, String content)
            throws FirebaseMessagingException;
}

package com.hcmus.mentor.backend.usercase.common.service;

import com.google.firebase.messaging.FirebaseMessagingException;

public interface ReminderService {
  void sendReminders() throws FirebaseMessagingException;
}

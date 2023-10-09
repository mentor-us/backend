package com.hcmus.mentor.backend.service;

import com.google.firebase.messaging.FirebaseMessagingException;

public interface ReminderService {
  void sendReminders() throws FirebaseMessagingException;
}

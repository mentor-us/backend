package vn.edu.hcmus.mentor.service;

import com.google.firebase.messaging.FirebaseMessagingException;

public interface ReminderService {

    void sendReminders() throws FirebaseMessagingException;
}

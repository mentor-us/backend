package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.domain.method.IRemindable;

/**
 * IRemindableService
 */
public interface IRemindableService {
    // List<Reminder> findReminderToday();
    void saveToReminder(IRemindable remindable);
}
package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.domain.method.IRemindable;

public interface IRemindableService {
    // List<Reminder> findReminderToday();
    void saveToReminder(IRemindable remindable);
}

package vn.edu.hcmus.mentor.backend.service;

import vn.edu.hcmus.mentor.backend.domain.method.IRemindable;

/**
 * IRemindableService
 */
public interface IRemindableService {
    // List<Reminder> findReminderToday();
    void saveToReminder(IRemindable remindable);
}
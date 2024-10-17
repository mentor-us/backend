package vn.edu.hcmus.mentor.service;

import vn.edu.hcmus.mentor.domain.method.IRemindable;

/**
 * IRemindableService
 */
public interface IRemindableService {
    // List<Reminder> findReminderToday();
    void saveToReminder(IRemindable remindable);
}
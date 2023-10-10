package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.entity.IRemindable;

public interface IRemindableService {
  // List<Reminder> findReminderToday();
  void saveToReminder(IRemindable remindable);
}

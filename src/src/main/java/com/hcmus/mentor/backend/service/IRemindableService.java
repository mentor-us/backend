package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.entity.IRemindable;
import com.hcmus.mentor.backend.entity.Reminder;

import java.util.List;

public interface IRemindableService {
    //List<Reminder> findReminderToday();
    void saveToReminder(IRemindable remindable);
}

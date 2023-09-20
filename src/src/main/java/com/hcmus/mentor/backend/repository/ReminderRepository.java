package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.entity.Reminder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface ReminderRepository extends MongoRepository<Reminder, String> {
    List<Reminder> findByReminderDateBefore(Date date);
    Boolean existsByRemindableId(String id);
    void deleteByRemindableId(String id);
    Reminder findByRemindableId(String id);
}
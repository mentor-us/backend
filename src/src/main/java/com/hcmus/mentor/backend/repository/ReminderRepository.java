package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Reminder;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReminderRepository extends MongoRepository<Reminder, String> {
    List<Reminder> findByReminderDateBefore(Date date);

    Boolean existsByRemindableId(String id);

    void deleteByRemindableId(String id);

    Reminder findByRemindableId(String id);
}

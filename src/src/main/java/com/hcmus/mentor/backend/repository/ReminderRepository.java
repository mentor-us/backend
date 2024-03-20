package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, String> {
    List<Reminder> findByReminderDateBefore(Date date);

    Boolean existsByRemindableId(String id);

    void deleteByRemindableId(String id);

    Reminder findByRemindableId(String id);
}

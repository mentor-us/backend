package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, String> {

    Reminder findByRemindableId(String id);

    List<Reminder> findByReminderDateBefore(Date date);

    void deleteByRemindableId(String id);
}
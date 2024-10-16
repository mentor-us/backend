package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.MeetingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingHistoryRepository extends JpaRepository<MeetingHistory, String> {
}
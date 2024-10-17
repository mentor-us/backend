package vn.edu.hcmus.mentor.backend.repository;

import vn.edu.hcmus.mentor.backend.domain.MeetingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingHistoryRepository extends JpaRepository<MeetingHistory, String> {
}
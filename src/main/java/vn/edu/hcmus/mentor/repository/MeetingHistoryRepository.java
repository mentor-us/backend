package vn.edu.hcmus.mentor.repository;

import vn.edu.hcmus.mentor.domain.MeetingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingHistoryRepository extends JpaRepository<MeetingHistory, String> {
}
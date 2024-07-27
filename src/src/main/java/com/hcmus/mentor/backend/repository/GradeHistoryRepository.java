package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.GradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeHistoryRepository extends JpaRepository<GradeHistory, String> {
}
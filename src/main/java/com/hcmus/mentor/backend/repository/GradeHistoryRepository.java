package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.GradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradeHistoryRepository extends JpaRepository<GradeHistory, String> {

    @Query(value = "SELECT gh.* " +
            "FROM grade_histories gh LEFT JOIN grade_versions gv on gh.grade_version_id = gv.id AND gv.user_id = ?4 AND gv.id != ?6 " +
            "WHERE (gh.year = ?1 AND gh.semester = ?2 AND gh.course_code = ?3) OR gh.id = ?5 " +
            "ORDER BY gh.updated_date DESC " +
            "LIMIT 1 ", nativeQuery = true)
    Optional<GradeHistory> findLastVersionOfGrade(String year, Integer semester, String courseCode, String userId, String gradeId, String gradeVersionId);
}
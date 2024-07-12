package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Grade;
import com.hcmus.mentor.backend.repository.custom.GradeRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<Grade, String>, GradeRepositoryCustom {
    @Query(value = "SELECT exists ( " +
            "SELECT  g.id " +
            "FROM grades g " +
            "   JOIN users u ON u.id = g.student_id " +
            "   LEFT JOIN list_mentors lm ON lm.mentee_id = u.id " +
            "   LEFT JOIN grade_user_access gua ON u.id = gua.user_id  " +
            "   LEFT JOIN user_roles ur ON u.id = ur.user_id " +
            "WHERE g.id = ?1 " +
            "   AND ( ur.roles = 0 " +
            "       OR u.grade_share_type = 'PUBLIC' " +
            "       OR ( u.grade_share_type = 'MENTOR' AND lm.mentor_id = ?2 ) " +
            "       OR gua.user_access_id = ?2 ))",
            nativeQuery = true)
    boolean canAccessGrade(String gradeId, String viewerId);
}
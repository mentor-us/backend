package vn.edu.hcmus.mentor.repository;

import vn.edu.hcmus.mentor.domain.GradeUserAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeUserAccessRepository extends JpaRepository<GradeUserAccess, String> {
    List<GradeUserAccess> findByUserId(String userId);
}
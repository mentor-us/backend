package vn.edu.hcmus.mentor.backend.repository;

import vn.edu.hcmus.mentor.backend.domain.GradeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeVersionRepository extends JpaRepository<GradeVersion, String> {

    List<GradeVersion> findByUserIdOrderByCreatedDateDesc(String userId);
}
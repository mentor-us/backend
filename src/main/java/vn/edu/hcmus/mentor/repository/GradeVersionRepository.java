package vn.edu.hcmus.mentor.repository;

import vn.edu.hcmus.mentor.domain.GradeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeVersionRepository extends JpaRepository<GradeVersion, String> {

    List<GradeVersion> findByUserIdOrderByCreatedDateDesc(String userId);
}
package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.GradeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeVersionRepository extends JpaRepository<GradeVersion, String> {
}
package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Grade;
import com.hcmus.mentor.backend.repository.custom.GradeRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<Grade, String>, GradeRepositoryCustom {
}
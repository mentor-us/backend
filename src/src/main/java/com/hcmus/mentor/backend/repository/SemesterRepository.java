package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Semester;
import com.hcmus.mentor.backend.repository.custom.SemesterRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, String>, SemesterRepositoryCustom {
    
    boolean existsByName(String name);
}
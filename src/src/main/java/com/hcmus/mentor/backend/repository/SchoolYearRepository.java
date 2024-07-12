package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.SchoolYear;
import com.hcmus.mentor.backend.repository.custom.SchoolYearRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolYearRepository extends JpaRepository<SchoolYear, String>, SchoolYearRepositoryCustom {

    boolean existsByName(String name);
}
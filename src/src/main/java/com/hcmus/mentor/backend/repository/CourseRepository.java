package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Course;
import com.hcmus.mentor.backend.repository.custom.CourseRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, String>, CourseRepositoryCustom {
}
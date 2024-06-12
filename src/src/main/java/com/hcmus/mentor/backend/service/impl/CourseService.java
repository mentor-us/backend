package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.usecase.course.search.SearchCourseQuery;
import com.hcmus.mentor.backend.domain.Course;
import com.hcmus.mentor.backend.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public Page<Course> search(SearchCourseQuery query) {
        return courseRepository.search(query);
    }

    public Optional<Course> findById(String id) {
        return courseRepository.findById(id);
    }

    public Course create(Course course) {
        return courseRepository.save(course);
    }

    public Course update(Course course) {
        return courseRepository.save(course);
    }

    public void delete(Course course) {
        courseRepository.delete(course);
    }
}

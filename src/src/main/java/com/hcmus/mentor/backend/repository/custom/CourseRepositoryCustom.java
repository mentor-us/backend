package com.hcmus.mentor.backend.repository.custom;

import com.hcmus.mentor.backend.controller.usecase.course.search.SearchCourseQuery;
import com.hcmus.mentor.backend.domain.Course;
import org.springframework.data.domain.Page;

public interface CourseRepositoryCustom {

    Page<Course> search(SearchCourseQuery query);
}

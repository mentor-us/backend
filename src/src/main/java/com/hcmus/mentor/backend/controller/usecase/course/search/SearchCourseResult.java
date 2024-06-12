package com.hcmus.mentor.backend.controller.usecase.course.search;

import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryResult;
import com.hcmus.mentor.backend.controller.usecase.course.common.CourseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SearchCourseResult extends PageQueryResult<CourseDto> {
}

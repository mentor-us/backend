package com.hcmus.mentor.backend.controller.usecase.course.search;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SearchCourseQuery extends PageQueryFilter implements Command<SearchCourseResult> {
}
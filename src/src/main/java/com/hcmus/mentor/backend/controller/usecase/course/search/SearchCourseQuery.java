package com.hcmus.mentor.backend.controller.usecase.course.search;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class SearchCourseQuery extends PageQueryFilter implements Command<SearchCourseResult> {
}

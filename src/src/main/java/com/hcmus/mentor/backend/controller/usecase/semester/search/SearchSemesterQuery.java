package com.hcmus.mentor.backend.controller.usecase.semester.search;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SearchSemesterQuery extends PageQueryFilter implements Command<SearchSemesterResult> {
}

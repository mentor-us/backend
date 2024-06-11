package com.hcmus.mentor.backend.controller.usecase.schoolyear.search;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SearchSchoolYearQuery extends PageQueryFilter implements Command<SearchSchoolYearResult> {
}

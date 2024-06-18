package com.hcmus.mentor.backend.controller.usecase.schoolyear.search;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SearchSchoolYearQuery extends PageQueryFilter implements Command<SearchSchoolYearResult> {
}
package com.hcmus.mentor.backend.controller.usecase.grade.getgrade;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchGradeQuery extends PageQueryFilter implements Command<SearchGradeResult> {

    private String userId;
}
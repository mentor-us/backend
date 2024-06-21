package com.hcmus.mentor.backend.controller.usecase.grade.getgrade;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchGradeQuery extends PageQueryFilter implements Command<SearchGradeResult> {

    private String userId;
    private String courseId;
    private String semesterId;
    private String yearId;
    private boolean isRetake;
}
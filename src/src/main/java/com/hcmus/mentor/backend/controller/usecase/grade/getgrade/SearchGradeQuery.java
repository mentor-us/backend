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
    private String courseName;
    private String courseCode;
    private Integer semester;
    private String year;
    private Boolean isRetake;
}
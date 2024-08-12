package com.hcmus.mentor.backend.controller.usecase.grade.updategrade;

import an.awesome.pipelinr.Command;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGradeCommand implements Command<GradeDto> {

    @JsonIgnoreProperties
    private String id;
    private Double score;
    private String value;
    private String studentId;
    private Integer semester;
    private String year;
    private String courseName;
    private String courseCode;
    private Boolean isRetake;
}
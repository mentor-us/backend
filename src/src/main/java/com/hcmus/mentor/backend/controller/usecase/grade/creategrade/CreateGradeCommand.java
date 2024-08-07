package com.hcmus.mentor.backend.controller.usecase.grade.creategrade;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGradeCommand implements Command<GradeDto> {

    private Double score = 0.0;
    private String value;
    private String studentId;
    private String courseName;
    private String courseCode;
    private Integer semester;
    private String year;
    @Builder.Default
    private Boolean isRetake = false;
}
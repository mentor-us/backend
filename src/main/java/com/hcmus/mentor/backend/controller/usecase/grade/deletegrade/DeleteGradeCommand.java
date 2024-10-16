package com.hcmus.mentor.backend.controller.usecase.grade.deletegrade;

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
public class DeleteGradeCommand implements Command<GradeDto> {

    private String id;
}
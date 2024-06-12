package com.hcmus.mentor.backend.controller.usecase.semester.update;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.semester.common.SemesterDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSemesterCommand implements Command<SemesterDto> {

    private String id;
    private String name;
}
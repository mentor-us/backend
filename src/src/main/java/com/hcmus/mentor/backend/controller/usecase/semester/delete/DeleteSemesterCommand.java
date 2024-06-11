package com.hcmus.mentor.backend.controller.usecase.semester.delete;

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
public class DeleteSemesterCommand implements Command<SemesterDto> {

    private String id;
}
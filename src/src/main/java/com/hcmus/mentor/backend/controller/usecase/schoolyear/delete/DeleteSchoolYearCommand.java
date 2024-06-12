package com.hcmus.mentor.backend.controller.usecase.schoolyear.delete;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.schoolyear.common.SchoolYearDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteSchoolYearCommand implements Command<SchoolYearDto> {

    private String id;
}
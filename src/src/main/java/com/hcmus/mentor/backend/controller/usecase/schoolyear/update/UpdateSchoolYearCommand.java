package com.hcmus.mentor.backend.controller.usecase.schoolyear.update;

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
public class UpdateSchoolYearCommand implements Command<SchoolYearDto> {

    private String id;
    private String name;
}
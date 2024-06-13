package com.hcmus.mentor.backend.controller.usecase.course.update;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.course.common.CourseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseCommand implements Command<CourseDto> {

    private String id;
    private String name;
    private String code;
}

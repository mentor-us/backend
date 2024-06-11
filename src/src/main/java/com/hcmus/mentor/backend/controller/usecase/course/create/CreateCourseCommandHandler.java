package com.hcmus.mentor.backend.controller.usecase.course.create;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.course.common.CourseDto;
import com.hcmus.mentor.backend.domain.Course;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.impl.CourseService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateCourseCommandHandler implements Command.Handler<CreateCourseCommand, CourseDto> {

    private final Logger logger = LoggerFactory.getLogger(CreateCourseCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final CourseService courseService;

    @Override
    public CourseDto handle(CreateCourseCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var course = modelMapper.map(command, Course.class);

        course = courseService.create(course);

        logger.info("User {} create course with command: {}", currentUserId, command);

        return modelMapper.map(course, CourseDto.class);
    }
}

package com.hcmus.mentor.backend.controller.usecase.course.update;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.course.common.CourseDto;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.impl.CourseService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateCourseCommandHandler implements Command.Handler<UpdateCourseCommand, CourseDto> {

    private final Logger logger = LoggerFactory.getLogger(UpdateCourseCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final CourseService courseService;

    @Override
    public CourseDto handle(UpdateCourseCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var course = courseService.findById(command.getId()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy môn học với id %s", command.getId())));

        var isUpdated = false;

        if (!course.getName().equals(command.getName())) {
            course.setName(command.getName());
            isUpdated = true;
        }

        if (!course.getCode().equals(command.getCode())) {
            course.setCode(command.getCode());
            isUpdated = true;
        }

        if (isUpdated) {
            courseService.update(course);

            logger.info("User {} updated course {}", currentUserId, command);
        }

        return modelMapper.map(course, CourseDto.class);
    }
}

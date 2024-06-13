package com.hcmus.mentor.backend.controller.usecase.course.delete;

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
public class DeleteCourseCommandHandler implements Command.Handler<DeleteCourseCommand, CourseDto> {

    private final Logger logger = LoggerFactory.getLogger(DeleteCourseCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final CourseService courseService;

    @Override
    public CourseDto handle(DeleteCourseCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var course = courseService.findById(command.getId()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy môn học với id %s", command.getId())));

        courseService.delete(course);

        logger.info("User {} deleted course {}", currentUserId, command);

        return modelMapper.map(course, CourseDto.class);
    }
}

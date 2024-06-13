package com.hcmus.mentor.backend.controller.usecase.course.getbyid;

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
public class GetCourseByIdQueryHandler implements Command.Handler<GetCourseByIdQuery, CourseDto> {

    private final Logger logger = LoggerFactory.getLogger(GetCourseByIdQueryHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final CourseService courseService;

    @Override
    public CourseDto handle(GetCourseByIdQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var course = courseService.findById(query.getId()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy môn học với id %s", query.getId())));

        logger.debug("User {} get course by id: {}", currentUserId, query.getId());

        return modelMapper.map(course, CourseDto.class);
    }
}

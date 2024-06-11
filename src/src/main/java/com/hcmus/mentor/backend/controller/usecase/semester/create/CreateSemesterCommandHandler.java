package com.hcmus.mentor.backend.controller.usecase.semester.create;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.semester.common.SemesterDto;
import com.hcmus.mentor.backend.domain.Semester;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.impl.SemesterService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateSemesterCommandHandler implements Command.Handler<CreateSemesterCommand, SemesterDto> {

    private final Logger logger = LoggerFactory.getLogger(CreateSemesterCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final SemesterService semesterService;

    @Override
    public SemesterDto handle(CreateSemesterCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var semester = modelMapper.map(command, Semester.class);

        semester = semesterService.create(semester);

        logger.info("User {} create semester with command: {}", currentUserId, command);

        return modelMapper.map(semester, SemesterDto.class);
    }
}
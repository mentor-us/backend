package com.hcmus.mentor.backend.controller.usecase.semester.update;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.semester.common.SemesterDto;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.impl.SemesterService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateSemesterCommandHandler implements Command.Handler<UpdateSemesterCommand, SemesterDto> {

    private final Logger logger = LoggerFactory.getLogger(UpdateSemesterCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final SemesterService semesterService;

    @Override
    public SemesterDto handle(UpdateSemesterCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var semester = semesterService.findById(command.getId()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy học kỳ với id %s", command.getId())));

        var isUpdated = false;

        if (!semester.getName().equals(command.getName())) {
            semester.setName(command.getName());
            isUpdated = true;
        }

        if (isUpdated) {
            semesterService.update(semester);

            logger.info("User {} updated semester {}", currentUserId, command);
        }

        return modelMapper.map(semester, SemesterDto.class);
    }
}
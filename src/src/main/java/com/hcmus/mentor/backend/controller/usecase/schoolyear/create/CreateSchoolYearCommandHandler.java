package com.hcmus.mentor.backend.controller.usecase.schoolyear.create;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.schoolyear.common.SchoolYearDto;
import com.hcmus.mentor.backend.domain.SchoolYear;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.impl.SchoolYearService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateSchoolYearCommandHandler implements Command.Handler<CreateSchoolYearCommand, SchoolYearDto> {

    private final Logger logger = LoggerFactory.getLogger(CreateSchoolYearCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final SchoolYearService schoolYearService;

    @Override
    public SchoolYearDto handle(CreateSchoolYearCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        if (schoolYearService.exists(command.getName())) {
            throw new DomainException("Năm học đã tồn tại");
        }

        var schoolYear = modelMapper.map(command, SchoolYear.class);

        schoolYear = schoolYearService.create(schoolYear);

        logger.info("User {} create school year with command: {}", currentUserId, command);

        return modelMapper.map(schoolYear, SchoolYearDto.class);
    }
}
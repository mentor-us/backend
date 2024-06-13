package com.hcmus.mentor.backend.controller.usecase.schoolyear.delete;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.schoolyear.common.SchoolYearDto;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.impl.SchoolYearService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteSchoolYearCommandHandler implements Command.Handler<DeleteSchoolYearCommand, SchoolYearDto> {

    private final Logger logger = LoggerFactory.getLogger(DeleteSchoolYearCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final SchoolYearService schoolYearService;

    @Override
    public SchoolYearDto handle(DeleteSchoolYearCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var schoolYear = schoolYearService.findById(command.getId()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy năm học với id %s", command.getId())));

        schoolYearService.delete(schoolYear);

        logger.info("User {} deleted school year {}", currentUserId, command);

        return modelMapper.map(schoolYear, SchoolYearDto.class);
    }
}
package com.hcmus.mentor.backend.controller.usecase.schoolyear.update;

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
public class UpdateSchoolYearCommandHandler implements Command.Handler<UpdateSchoolYearCommand, SchoolYearDto> {

    private final Logger logger = LoggerFactory.getLogger(UpdateSchoolYearCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final SchoolYearService schoolYearService;

    @Override
    public SchoolYearDto handle(UpdateSchoolYearCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var schoolYear = schoolYearService.findById(command.getId()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy năm học với id %s", command.getId())));

        var isUpdated = false;

        if (!schoolYear.getName().equals(command.getName())) {
            schoolYear.setName(command.getName());
            isUpdated = true;
        }

        if (isUpdated) {
            schoolYearService.update(schoolYear);

            logger.info("User {} updated school year {}", currentUserId, command);
        }

        return modelMapper.map(schoolYear, SchoolYearDto.class);
    }
}

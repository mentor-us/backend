package com.hcmus.mentor.backend.controller.usecase.schoolyear.getbyid;

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
public class GetSchoolYearByIdQueryHandler implements Command.Handler<GetSchoolYearByIdQuery, SchoolYearDto> {

    private final Logger logger = LoggerFactory.getLogger(GetSchoolYearByIdQueryHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final SchoolYearService schoolYearService;

    @Override
    public SchoolYearDto handle(GetSchoolYearByIdQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var schoolYear = schoolYearService.findById(query.getId()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy năm học với id %s", query.getId())));

        logger.debug("User {} get school year by id: {}", currentUserId, query.getId());

        return modelMapper.map(schoolYear, SchoolYearDto.class);
    }
}
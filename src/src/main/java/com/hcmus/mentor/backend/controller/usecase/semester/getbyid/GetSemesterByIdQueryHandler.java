package com.hcmus.mentor.backend.controller.usecase.semester.getbyid;

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
public class GetSemesterByIdQueryHandler implements Command.Handler<GetSemesterByIdQuery, SemesterDto> {

    private final Logger logger = LoggerFactory.getLogger(GetSemesterByIdQueryHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final SemesterService semesterService;

    @Override
    public SemesterDto handle(GetSemesterByIdQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var semester = semesterService.findById(query.getId()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy học kỳ với id %s", query.getId())));

        logger.debug("User {} get semester by id: {}", currentUserId, query.getId());

        return modelMapper.map(semester, SemesterDto.class);
    }
}
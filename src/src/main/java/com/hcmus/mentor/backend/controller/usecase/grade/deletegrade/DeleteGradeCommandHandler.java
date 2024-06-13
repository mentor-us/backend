package com.hcmus.mentor.backend.controller.usecase.grade.deletegrade;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.impl.GradeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteGradeCommandHandler implements Command.Handler<DeleteGradeCommand, GradeDto> {

    private final Logger logger = LoggerFactory.getLogger(DeleteGradeCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final GradeService gradeService;

    @Override
    public GradeDto handle(DeleteGradeCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var grade = gradeService.findById(command.getId()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy điểm với id %s", command.getId())));

        gradeService.delete(grade);

        logger.info("User {} delete grade with id: {}", currentUserId, command.getId());

        return modelMapper.map(grade, GradeDto.class);
    }
}
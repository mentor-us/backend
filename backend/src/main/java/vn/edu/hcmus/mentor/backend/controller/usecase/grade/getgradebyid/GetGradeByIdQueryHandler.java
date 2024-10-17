package vn.edu.hcmus.mentor.backend.controller.usecase.grade.getgradebyid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.backend.controller.exception.DomainException;
import vn.edu.hcmus.mentor.backend.controller.exception.ForbiddenException;
import vn.edu.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import vn.edu.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import vn.edu.hcmus.mentor.backend.service.impl.GradeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GetGradeByIdQueryHandler implements Command.Handler<GetGradeByIdQuery, GradeDto> {

    private final Logger logger = LoggerFactory.getLogger(GetGradeByIdQueryHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final GradeService gradeService;

    @Override
    @Transactional(readOnly = true)
    public GradeDto handle(GetGradeByIdQuery command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var grade = gradeService.findById(command.getId()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy điểm với id %s", command.getId())));
        if (!gradeService.canAccessUser(grade.getStudent().getId(), currentUserId)) {
            throw new ForbiddenException("Không có quyền truy cập");
        }

        logger.debug("User {} get grade with id: {}", currentUserId, command.getId());

        return modelMapper.map(grade, GradeDto.class);
    }
}
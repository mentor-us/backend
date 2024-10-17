package vn.edu.hcmus.mentor.controller.usecase.grade.deletegrade;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.exception.DomainException;
import vn.edu.hcmus.mentor.controller.usecase.grade.common.GradeDto;
import vn.edu.hcmus.mentor.domain.AuditRecord;
import vn.edu.hcmus.mentor.domain.constant.ActionType;
import vn.edu.hcmus.mentor.domain.constant.DomainType;
import vn.edu.hcmus.mentor.repository.GradeHistoryRepository;
import vn.edu.hcmus.mentor.repository.UserRepository;
import vn.edu.hcmus.mentor.security.principal.LoggedUserAccessor;
import vn.edu.hcmus.mentor.service.AuditRecordService;
import vn.edu.hcmus.mentor.service.impl.GradeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteGradeCommandHandler implements Command.Handler<DeleteGradeCommand, GradeDto> {

    private final Logger logger = LoggerFactory.getLogger(DeleteGradeCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final GradeService gradeService;
    private final AuditRecordService auditRecordService;
    private final UserRepository userRepository;
    private final GradeHistoryRepository gradeHistoryRepository;

    @Override
    @Transactional
    public GradeDto handle(DeleteGradeCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var grade = gradeService.findById(command.getId()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy điểm với id %s", command.getId())));

        gradeService.delete(grade);
        auditRecordService.save(AuditRecord.builder()
                .action(ActionType.DELETED)
                .domain(DomainType.GRADE)
                .entityId(grade.getId())
                .detail(String.format("Xóa điểm cho sinh viên %s với môn học: %s, học kỳ: %d, năm học: %s, điểm: %f", grade.getStudent().getName(), grade.getCourseName(), grade.getSemester(), grade.getYear(), grade.getScore()))
                .user(userRepository.findById(currentUserId).orElse(null))
                .build());

        gradeHistoryRepository.findById(grade.getId()).ifPresent(gradeHistoryRepository::delete);

        logger.info("User {} delete grade with id: {}", currentUserId, command.getId());

        return modelMapper.map(grade, GradeDto.class);
    }
}
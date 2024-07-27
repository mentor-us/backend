package com.hcmus.mentor.backend.controller.usecase.grade.creategrade;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.Grade;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.AuditRecordService;
import com.hcmus.mentor.backend.service.impl.GradeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateGradeCommandHandler implements Command.Handler<CreateGradeCommand, GradeDto> {

    private final Logger logger = LoggerFactory.getLogger(CreateGradeCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final GradeService gradeService;
    private final UserRepository userRepository;
    private final AuditRecordService auditRecordService;

    @Override
    public GradeDto handle(CreateGradeCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var student = userRepository.findById(command.getStudentId()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy sinh viên với id %s", command.getStudentId())));
        var creator = userRepository.findById(currentUserId).orElseThrow(() -> new DomainException(String.format("Không tìm thấy người dùng với id %s", currentUserId)));

        var grade = modelMapper.map(command, Grade.class);
        grade.setStudent(student);
        grade.setCreator(creator);
        grade.setSemester(command.getSemester());
        grade.setYear(command.getYear());
        grade.setCourseCode(command.getCourseCode());
        grade.setCourseName(command.getCourseName());
        grade.setIsRetake(command.getIsRetake());
        grade.setScore(command.getScore());
        grade.setValue(command.getValue());

        grade = gradeService.create(grade);

        auditRecordService.save(AuditRecord.builder()
                .action(ActionType.CREATED)
                .domain(DomainType.GRADE)
                .entityId(grade.getId())
                .detail(String.format("Thêm điểm cho sinh viên %s với môn học: %s, học kỳ: %d, năm học: %s, điểm: %f", student.getName(), grade.getCourseName(), grade.getSemester(), grade.getYear(), grade.getScore()))
                .user(creator)
                .build());

        logger.info("User {} create grade with command: {}", currentUserId, command);

        return modelMapper.map(grade, GradeDto.class);
    }
}
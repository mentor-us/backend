package com.hcmus.mentor.backend.controller.usecase.grade.creategrade;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import com.hcmus.mentor.backend.controller.usecase.grade.updategrade.UpdateGradeCommand;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.Grade;
import com.hcmus.mentor.backend.domain.GradeHistory;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import com.hcmus.mentor.backend.repository.GradeHistoryRepository;
import com.hcmus.mentor.backend.repository.GradeRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.AuditRecordService;
import com.hcmus.mentor.backend.service.impl.GradeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreateGradeCommandHandler implements Command.Handler<CreateGradeCommand, GradeDto> {

    private final Logger logger = LoggerFactory.getLogger(CreateGradeCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final GradeService gradeService;
    private final UserRepository userRepository;
    private final AuditRecordService auditRecordService;
    private final GradeHistoryRepository gradeHistoryRepository;
    private final GradeRepository gradeRepository;
    private final Pipeline pipeline;

    @Override
    @Transactional
    public GradeDto handle(CreateGradeCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var student = userRepository.findById(command.getStudentId())
                .orElseThrow(() -> new DomainException(String.format("Không tìm thấy sinh viên với id %s", command.getStudentId())));
        var creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new DomainException(String.format("Không tìm thấy người dùng với id %s", currentUserId)));

        var grade = gradeRepository.findByYearAndSemesterAndCourseCodeAndStudentId(command.getYear(), command.getSemester(), command.getCourseCode(), command.getStudentId())
                .orElse(Grade.builder()
                        .student(student)
                        .creator(creator)
                        .semester(command.getSemester())
                        .year(command.getYear())
                        .courseCode(command.getCourseCode())
                        .courseName(command.getCourseName())
                        .isRetake(command.getIsRetake())
                        .build());
        GradeDto gradeDto;

        if (grade.getId() == null) {
            grade = gradeService.create(grade);
            gradeHistoryRepository.save(modelMapper.map(grade, GradeHistory.class));
            auditRecordService.save(AuditRecord.builder()
                    .action(ActionType.CREATED)
                    .domain(DomainType.GRADE)
                    .entityId(grade.getId())
                    .detail(String.format("Thêm điểm cho sinh viên %s với môn học: %s, học kỳ: %d, năm học: %s, điểm: %f",
                            student.getName(), grade.getCourseName(), grade.getSemester(), grade.getYear(), grade.getScore()))
                    .user(creator)
                    .build());

            gradeDto = modelMapper.map(grade, GradeDto.class);

            logger.info("User {} create grade with command: {}", currentUserId, command);
        } else {
            gradeDto = pipeline.send(UpdateGradeCommand.builder()
                    .id(grade.getId())
                    .score(command.getScore())
                    .value(command.getValue())
                    .courseName(command.getCourseName())
                    .build());
        }

        return gradeDto;
    }
}
package com.hcmus.mentor.backend.controller.usecase.grade.updategrade;


import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.GradeHistory;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import com.hcmus.mentor.backend.repository.GradeHistoryRepository;
import com.hcmus.mentor.backend.repository.GradeVersionRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.AuditRecordService;
import com.hcmus.mentor.backend.service.impl.GradeService;
import com.hcmus.mentor.backend.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateGradeCommandHandler implements Command.Handler<UpdateGradeCommand, GradeDto> {

    private final Logger logger = LoggerFactory.getLogger(UpdateGradeCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final GradeService gradeService;
    private final UserRepository userRepository;
    private final AuditRecordService auditRecordService;
    private final GradeHistoryRepository gradeHistoryRepository;
    private final GradeVersionRepository gradeVersionRepository;

    @Override
    @Transactional
    public GradeDto handle(UpdateGradeCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var grade = gradeService.findById(command.getId()).orElseThrow(()
                -> new DomainException(String.format("Không tìm thấy điểm với id %s", command.getId())));

        var detail = new StringBuilder();
        var isUpdateScore = false;

        if (command.getScore() != null && !command.getScore().equals(grade.getScore())) {
            grade.setScore(command.getScore());
            detail.append("\n").append(String.format("Điểm số: %f", grade.getScore()));
            isUpdateScore = true;
        }

        if (command.getValue() != null && !command.getValue().equals(grade.getValue())) {
            grade.setValue(command.getValue());
            detail.append("\n").append(String.format("Điểm chữ: %s", grade.getValue()));
            isUpdateScore = true;
        }

        if (command.getStudentId() != null && !grade.getStudent().getId().equals(command.getStudentId())) {
            var student = userRepository
                    .findById(command.getStudentId())
                    .orElseThrow(() -> new DomainException(String.format("Không tìm thấy sinh viên với id %s", command.getStudentId())));
            grade.setStudent(student);
            detail.append("\n").append(String.format("Sinh viên: %s", student.getName()));
            isUpdateScore = true;
        }

        if (command.getSemester() != null && !grade.getSemester().equals(command.getSemester())) {
            grade.setSemester(command.getSemester());
            detail.append("\n").append(String.format("Học kỳ: %d", grade.getSemester()));
            isUpdateScore = true;
        }

        if (command.getYear() != null && !grade.getYear().equals(command.getYear())) {
            grade.setYear(command.getYear());
            detail.append("\n").append(String.format("Năm học: %s", grade.getYear()));
            isUpdateScore = true;
        }

        if (command.getCourseCode() != null && !grade.getCourseCode().equals(command.getCourseCode())) {
            grade.setCourseCode(command.getCourseCode());
            detail.append("\n").append(String.format("Mã môn học: %s", grade.getCourseCode()));
            isUpdateScore = true;
        }

        if (command.getCourseName() != null && !grade.getCourseName().equals(command.getCourseName())) {
            grade.setCourseName(command.getCourseName());
            detail.append("\n").append(String.format("Tên môn học: %s", grade.getCourseName()));
            isUpdateScore = true;
        }

        if (command.getIsRetake() != null && grade.getIsRetake() != command.getIsRetake()) {
            grade.setIsRetake(command.getIsRetake());
            detail.append("\n").append(String.format("Là môn thi lại: %s", grade.getIsRetake()));
        }

        if (!detail.isEmpty()) {
            grade.setUpdatedDate(DateUtils.getDateNowAtUTC());
            grade.setGradeVersion(null);
            grade = gradeService.update(grade);
            auditRecordService.save(AuditRecord.builder()
                    .action(ActionType.UPDATED)
                    .domain(DomainType.GRADE)
                    .entityId(grade.getId())
                    .detail(String.format("Cập nhật điểm của sinh viên %s: %s", grade.getStudent().getEmail(), detail))
                    .user(userRepository.findById(currentUserId).orElse(null))
                    .build());
            logger.info("User {} update grade with command: {}", currentUserId, command);
        }

        if (isUpdateScore) {
            var gradeHistory = modelMapper.map(grade, GradeHistory.class);
            gradeHistory.setUpdatedDate(DateUtils.getDateNowAtUTC());
            gradeHistoryRepository.save(gradeHistory);
        }

        return modelMapper.map(grade, GradeDto.class);
    }
}
package com.hcmus.mentor.backend.controller.usecase.grade.updategrade;


import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.impl.GradeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateGradeCommandHandler implements Command.Handler<UpdateGradeCommand, GradeDto> {

    private final Logger logger = LoggerFactory.getLogger(UpdateGradeCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final GradeService gradeService;
    private final UserRepository userRepository;

    @Override
    public GradeDto handle(UpdateGradeCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var grade = gradeService.findById(command.getId()).orElseThrow(()
                -> new DomainException(String.format("Không tìm thấy điểm với id %s", command.getId())));

        var isUpdated = false;

        if (command.getScore() != null && !grade.getScore().equals(command.getScore())) {
            grade.setScore(command.getScore());
            isUpdated = true;
        }

        if (command.getStudent() != null && !grade.getStudent().getId().equals(command.getStudent())) {
            var student = userRepository.findById(command.getStudent()).orElseThrow(()
                    -> new DomainException(String.format("Không tìm thấy sinh viên với id %s", command.getStudent())));
            grade.setStudent(student);
            isUpdated = true;
        }

        if (command.getSemester() != null && !grade.getSemester().equals(command.getSemester())) {
            grade.setSemester(command.getSemester());
            isUpdated = true;
        }

        if (command.getYear() != null && !grade.getYear().equals(command.getYear())) {
            grade.setYear(command.getYear());
            isUpdated = true;
        }

        if (command.getCourseCode() != null && !grade.getCourseCode().equals(command.getCourseCode())) {
            grade.setCourseCode(command.getCourseCode());
            isUpdated = true;
        }

        if (command.getCourseName() != null && !grade.getCourseName().equals(command.getCourseName())) {
            grade.setCourseName(command.getCourseName());
            isUpdated = true;
        }

        if (command.getIsRetake() != null && grade.getIsRetake() != command.getIsRetake()) {
            grade.setIsRetake(command.getIsRetake());
            isUpdated = true;
        }

        if (isUpdated) {
            grade = gradeService.update(grade);
            logger.info("User {} update grade with command: {}", currentUserId, command);
        }

        return modelMapper.map(grade, GradeDto.class);
    }
}
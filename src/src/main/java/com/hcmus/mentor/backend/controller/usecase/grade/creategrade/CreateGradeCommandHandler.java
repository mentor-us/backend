package com.hcmus.mentor.backend.controller.usecase.grade.creategrade;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import com.hcmus.mentor.backend.domain.Grade;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.CourseRepository;
import com.hcmus.mentor.backend.repository.SchoolYearRepository;
import com.hcmus.mentor.backend.repository.SemesterRepository;
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
public class CreateGradeCommandHandler implements Command.Handler<CreateGradeCommand, GradeDto> {

    private final Logger logger = LoggerFactory.getLogger(CreateGradeCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final GradeService gradeService;
    private final UserRepository userRepository;
    private final SemesterRepository semesterRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final CourseRepository courseRepository;

    @Override
    public GradeDto handle(CreateGradeCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var student = userRepository.findById(command.getStudent()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy sinh viên vơi id %s", command.getStudent())));
        User creator;
        if (command.getCreator().equals(command.getStudent())) {
            creator = student;
        } else {
            creator = userRepository.findById(command.getCreator()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy người tạo vơi id %s", command.getCreator())));
        }

        var semester = semesterRepository.findById(command.getSemester()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy học kỳ vơi id %s", command.getSemester())));
        var schoolYear = schoolYearRepository.findById(command.getYear()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy năm học vơi id %s", command.getYear())));
        var course = courseRepository.findById(command.getCourse()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy môn học vơi id %s", command.getCourse())));

        var grade = modelMapper.map(command, Grade.class);
        grade.setStudent(student);
        grade.setCreator(creator);
        grade.setSemester(semester);
        grade.setYear(schoolYear);
        grade.setCourse(course);

        grade = gradeService.create(grade);

        logger.info("User {} create grade with command: {}", currentUserId, command);

        return modelMapper.map(grade, GradeDto.class);
    }
}
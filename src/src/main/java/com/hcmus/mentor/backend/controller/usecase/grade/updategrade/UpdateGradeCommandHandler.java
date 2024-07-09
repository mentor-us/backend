package com.hcmus.mentor.backend.controller.usecase.grade.updategrade;


import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
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
public class UpdateGradeCommandHandler implements Command.Handler<UpdateGradeCommand, GradeDto> {

    private final Logger logger = LoggerFactory.getLogger(UpdateGradeCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final GradeService gradeService;
    private final UserRepository userRepository;
    private final SemesterRepository semesterRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final CourseRepository courseRepository;

    @Override
    public GradeDto handle(UpdateGradeCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var grade = gradeService.findById(command.getId()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy điểm với id %s", command.getId())));

        var isUpdated = false;

        if (command.getScore() != null && !grade.getScore().equals(command.getScore())) {
            grade.setScore(command.getScore());
            isUpdated = true;
        }

        if (command.getVerified() != null && grade.isVerified() != command.getVerified()) {
            grade.setScore(command.getScore());
        }

        if (command.getStudent() != null && !grade.getStudent().getId().equals(command.getStudent())) {
            var student = userRepository.findById(command.getStudent()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy sinh viên với id %s", command.getStudent())));
            grade.setStudent(student);
            isUpdated = true;
        }

        if (command.getCreator() != null && !grade.getCreator().getId().equals(command.getCreator())) {
            var creator = userRepository.findById(command.getCreator()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy người tạo điểm với id %s", command.getCreator())));
            grade.setCreator(creator);
            isUpdated = true;
        }

        if (command.getSemester() != null && !grade.getSemester().getId().equals(command.getSemester())) {
            var semester = semesterRepository.findById(command.getSemester()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy học kỳ với id %s", command.getSemester())));
            grade.setSemester(semester);
            isUpdated = true;
        }

        if (command.getYear() != null && !grade.getYear().getId().equals(command.getYear())) {
            var year = schoolYearRepository.findById(command.getYear()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy năm học với id %s", command.getYear())));
            grade.setYear(year);
            isUpdated = true;
        }

        if (command.getCourse() != null && !grade.getCourse().getId().equals(command.getCourse())) {
            var course = courseRepository.findById(command.getCourse()).orElseThrow(() -> new DomainException(String.format("Không tìm thấy môn học với id %s", command.getCourse())));
            grade.setCourse(course);
            isUpdated = true;
        }

        if (isUpdated) {
            grade = gradeService.update(grade);

            logger.info("User {} update grade with command: {}", currentUserId, command);
        }

        return modelMapper.map(grade, GradeDto.class);
    }
}
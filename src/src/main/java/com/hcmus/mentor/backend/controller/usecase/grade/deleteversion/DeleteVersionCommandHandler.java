package com.hcmus.mentor.backend.controller.usecase.grade.deleteversion;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.domain.Grade;
import com.hcmus.mentor.backend.repository.GradeHistoryRepository;
import com.hcmus.mentor.backend.repository.GradeRepository;
import com.hcmus.mentor.backend.repository.GradeVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class DeleteVersionCommandHandler implements Command.Handler<DeleteVersionCommand, Void> {

    private final GradeRepository gradeRepository;
    private final GradeHistoryRepository gradeHistoryRepository;
    private final GradeVersionRepository gradeVersionRepository;

    @Override
    @Transactional
    public Void handle(DeleteVersionCommand command) {
        var gradeVersion = gradeVersionRepository.findById(command.getVersionId()).orElseThrow(() -> new DomainException("Không tìm thấy phiên bản"));
        var gradeHistories = gradeVersion.getHistories();
        var grades = gradeVersion.getGrades();

        gradeHistoryRepository.deleteAll(gradeHistories);
        var gradeDelete = new ArrayList<Grade>();
        grades.forEach(grade -> {
            gradeHistoryRepository.findLastVersionOfGrade(grade.getYear(), grade.getSemester(), grade.getCourseCode(), grade.getStudent().getId(), grade.getId(), gradeVersion.getId())
                    .ifPresentOrElse(gradeHistory -> {
                        grade.setValue(gradeHistory.getValue());
                        grade.setScore(gradeHistory.getScore());
                        grade.setCourseName(gradeHistory.getCourseName());
                        grade.setGradeVersion(gradeHistory.getGradeVersion());
                        grade.setUpdatedDate(gradeHistory.getUpdatedDate());
                        gradeRepository.save(grade);
                    }, () -> {
                        grades.remove(grade);
                        gradeDelete.add(grade);
                    });
        });
        gradeRepository.deleteAll(gradeDelete);
        gradeRepository.saveAll(grades);
        gradeVersionRepository.delete(gradeVersion);

        return null;
    }
}
package com.hcmus.mentor.backend.controller.usecase.grade.importgrade;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import com.hcmus.mentor.backend.domain.Grade;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.GradeRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ImportGradeCommandHandler implements Command.Handler<ImportGradeCommand, List<GradeDto>> {

    private final Logger logger = LoggerFactory.getLogger(ImportGradeCommandHandler.class);
    private final GradeRepository gradeRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;


    @SneakyThrows
    @Override
    public List<GradeDto> handle(ImportGradeCommand command) {
        var currentUser = userRepository.findById(loggedUserAccessor.getCurrentUserId())
                .orElseThrow(() -> new DomainException("Không tìm thấy người dùng"));
        var user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new DomainException("Không tìm thấy sinh viên"));

        List<Grade> grades = new ArrayList<>();

        try (InputStream data = command.getFile().getInputStream()) {
            Workbook workbook = new XSSFWorkbook(data);
            Sheet sheet = workbook.getSheetAt(0);
            processSheet(sheet, currentUser, user, grades);
        } catch (IOException e) {
            throw new DomainException(String.valueOf(e));
        }

        gradeRepository.saveAll(grades);

        return grades.stream()
                .map(grade -> modelMapper.map(grade, GradeDto.class))
                .collect(Collectors.toList());
    }

    private void processSheet(Sheet sheet, User currentUser, User student, List<Grade> grades) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            if (i < 11) continue;

            Row row = sheet.getRow(i);
            if (row == null) continue;

            var cellIterator = row.cellIterator();
            var yearCell = cellIterator.next();
            if (yearCell.getStringCellValue().contains("Năm học")) {
                String year = yearCell.getStringCellValue().substring(8).trim();
                Integer semester = getSemester(cellIterator);
                i += 2;
                i = processGrades(sheet, i, currentUser, student, grades, year, semester);
            }
        }
    }

    private Integer getSemester(Iterator<Cell> cellIterator) {
        while (cellIterator.hasNext()) {
            var cell = cellIterator.next();
            if (cell.getStringCellValue().contains("Học kỳ")) {
                return Integer.parseInt(cell.getStringCellValue().substring(9, 10));
            }
        }
        return null;
    }

    private int processGrades(Sheet sheet, int rowIndex, User currentUser, User student, List<Grade> grades, String year, Integer semester) {
        Row row = sheet.getRow(rowIndex);
        while (row != null) {
            var cellIterator = row.cellIterator();
            if (cellIterator.next().getCellType() == CellType.STRING) break;

            cellIterator.next();
            String courseCode = cellIterator.next().getStringCellValue();
            cellIterator.next();
            String courseName = cellIterator.next().getStringCellValue();
            skipCells(cellIterator, 7);
            double credit = cellIterator.next().getNumericCellValue();
            skipCells(cellIterator, 2);
            String gradeValue = getGradeValue(cellIterator.next());
            skipCells(cellIterator, 2);
            Double gradePoint = getGradePoint(cellIterator.next());

            if (gradeValue != null || gradePoint != null) {
                updateOrAddGrade(grades, year, semester, courseCode, courseName, gradeValue, gradePoint, student, currentUser);
            }

            rowIndex++;
            row = sheet.getRow(rowIndex);
        }
        return rowIndex;
    }

    private void skipCells(Iterator<Cell> cellIterator, int count) {
        for (int i = 0; i < count; i++) {
            if (cellIterator.hasNext()) cellIterator.next();
        }
    }

    private String getGradeValue(Cell cell) {
        return (cell.getCellType() == CellType.BLANK || cell.getStringCellValue().equals("(*)")) ? null : cell.getStringCellValue();
    }

    private Double getGradePoint(Cell cell) {
        return (cell.getCellType() == CellType.BLANK) ? null : cell.getNumericCellValue();
    }

    private void updateOrAddGrade(List<Grade> grades, String year, Integer semester, String courseCode, String courseName, String gradeValue, Double gradePoint, User student, User currentUser) {
        Optional.ofNullable(gradeRepository.findByYearAndSemesterAndCourseCodeAndStudentId(year, semester, courseCode, student.getId()))
                .ifPresentOrElse(grade -> {
                    grade.setScore(gradePoint);
                    grade.setValue(gradeValue);
                    grade.setCourseName(courseName);
                    grade.setCreator(currentUser);
                    grades.add(grade);
                }, () -> {
                    Grade grade = new Grade();
                    grade.setYear(year);
                    grade.setSemester(semester);
                    grade.setCourseCode(courseCode);
                    grade.setCourseName(courseName);
                    grade.setValue(gradeValue);
                    grade.setScore(gradePoint);
                    grade.setStudent(student);
                    grade.setCreator(currentUser);
                    grades.add(grade);
                });
    }

}
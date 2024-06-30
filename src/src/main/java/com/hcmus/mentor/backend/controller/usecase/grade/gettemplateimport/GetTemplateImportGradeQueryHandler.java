package com.hcmus.mentor.backend.controller.usecase.grade.gettemplateimport;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.domain.Course;
import com.hcmus.mentor.backend.domain.SchoolYear;
import com.hcmus.mentor.backend.domain.Semester;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.impl.CourseService;
import com.hcmus.mentor.backend.service.impl.SchoolYearService;
import com.hcmus.mentor.backend.service.impl.SemesterService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Component
@RequiredArgsConstructor
public class GetTemplateImportGradeQueryHandler implements Command.Handler<GetTemplateImportGradeQuery, GetTemplateImportGradeResult> {

    private final Logger logger = LoggerFactory.getLogger(GetTemplateImportGradeQueryHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final SchoolYearService schoolYearService;
    private final CourseService courseService;
    private final SemesterService semesterService;

    @SneakyThrows
    @Override
    public GetTemplateImportGradeResult handle(GetTemplateImportGradeQuery command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var schoolYears = schoolYearService.findAll();
        var courses = courseService.findAll();
        var semesters = semesterService.findAll();

        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Grade Data");
        var headerRow = sheet.createRow(0);

        headerRow.createCell(0).setCellValue("Year");
        headerRow.createCell(1).setCellValue("Semester");
        headerRow.createCell(2).setCellValue("Course");
        headerRow.createCell(3).setCellValue("User");
        headerRow.createCell(4).setCellValue("Score");

        var dvHelper = sheet.getDataValidationHelper();

        var schoolYearConstraint = dvHelper.createExplicitListConstraint(schoolYears.stream().map(SchoolYear::getName).toArray(String[]::new));
        var schoolYearAddressList = new CellRangeAddressList(1, 10000, 0, 0);
        var schoolYearValidation = dvHelper.createValidation(schoolYearConstraint, schoolYearAddressList);
        setProperty(schoolYearValidation);
        sheet.addValidationData(schoolYearValidation);

        var semesterConstraint = dvHelper.createExplicitListConstraint(semesters.stream().map(Semester::getName).toArray(String[]::new));
        var semesterAddressList = new CellRangeAddressList(1, 10000, 1, 1);
        var semesterValidation = dvHelper.createValidation(semesterConstraint, semesterAddressList);
        setProperty(semesterValidation);
        sheet.addValidationData(semesterValidation);

        var courseConstraint = dvHelper.createExplicitListConstraint(courses.stream().map(Course::getName).toArray(String[]::new));
        var courseAddressList = new CellRangeAddressList(1, 10000, 2, 2);
        var courseValidation = dvHelper.createValidation(courseConstraint, courseAddressList);
        setProperty(courseValidation);
        sheet.addValidationData(courseValidation);

        var outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        logger.debug("Get template import grade with UserId {}", currentUserId);

        var result = GetTemplateImportGradeResult.builder().stream(new ByteArrayInputStream(outputStream.toByteArray())).contentLength(outputStream.size()).build();
        outputStream.close();

        return result;
    }

    private void setProperty(DataValidation validation) {
        validation.setSuppressDropDownArrow(true);
        validation.setEmptyCellAllowed(false);
        validation.setShowPromptBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
    }
}

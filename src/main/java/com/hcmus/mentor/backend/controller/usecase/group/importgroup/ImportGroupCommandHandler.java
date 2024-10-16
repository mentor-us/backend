package com.hcmus.mentor.backend.controller.usecase.group.importgroup;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants;
import com.hcmus.mentor.backend.controller.usecase.group.creategroup.CreateGroupCommand;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domainservice.GroupDomainService;
import com.hcmus.mentor.backend.repository.GroupCategoryRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.service.ShareService;
import com.hcmus.mentor.backend.service.dto.GroupServiceDto;
import com.hcmus.mentor.backend.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Predicate;

import static com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants.SUCCESS;

/**
 * Handler for {@link ImportGroupCommand}
 */
@Component
@RequiredArgsConstructor
public class ImportGroupCommandHandler implements Command.Handler<ImportGroupCommand, GroupServiceDto> {

    private final Logger logger = LoggerFactory.getLogger(ImportGroupCommandHandler.class);
    private final LoggedUserAccessor loggedUserAccessor;
    private final PermissionService permissionService;
    private final ShareService shareService;
    private final GroupCategoryRepository groupCategoryRepository;
    private final Pipeline pipeline;
    private final GroupDomainService groupDomainService;

    /**
     * @param command command to import group
     * @return group service DTO
     */
    @Override
    public GroupServiceDto handle(final ImportGroupCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        if (!permissionService.isAdmin(currentUserId, 0)) {
            return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        List<CreateGroupCommand> commands;
        try (InputStream data = command.getFile().getInputStream();
             Workbook workbook = new XSSFWorkbook(data)) {
            List<String> nameHeaders = new ArrayList<>();
            nameHeaders.add("STT");
            nameHeaders.add("Loại nhóm *");
            nameHeaders.add("Emails người được quản lí *");
            nameHeaders.add("Tên nhóm *");
            nameHeaders.add("Mô tả");
            nameHeaders.add("Emails người quản lí *");
            nameHeaders.add("Ngày bắt đầu *\n" + "(dd/MM/YYYY)");
            nameHeaders.add("Ngày kết thúc *\n" + "(dd/MM/YYYY)");

            if (!shareService.isValidTemplate(workbook, 2, nameHeaders)) {
                return new GroupServiceDto(ReturnCodeConstants.GROUP_INVALID_TEMPLATE, "Invalid template", null);
            }

            GroupServiceDto validReadGroups = readGroups(workbook);
            if (!Objects.equals(validReadGroups.getReturnCode(), SUCCESS)) {
                return validReadGroups;
            }

            commands = (List<CreateGroupCommand>) validReadGroups.getData();
        } catch (ParseException | IOException e) {
            throw new DomainException(String.valueOf(e));
        }
        List<Group> groups = new ArrayList<>();
        for (var cm : commands) {
            GroupServiceDto returnData = pipeline.send(cm);
            if (!Objects.equals(returnData.getReturnCode(), SUCCESS)) {
                return returnData;
            }
            groups.add((Group) returnData.getData());
        }

        logger.info("Imported groups successfully, name: {}", groups.stream().map(Group::getName).toList());

        return new GroupServiceDto(SUCCESS, null, groups);
    }

    private GroupServiceDto readGroups(Workbook workbook) throws ParseException {
        Map<String, CreateGroupCommand> commands = new HashMap<>();

        Sheet sheet = workbook.getSheet("Data");
        removeBlankRows(sheet);
        String groupCategoryName;
        List<String> menteeEmails;
        List<String> mentorEmails;
        String groupName;
        String description = "";
        LocalDateTime timeStart = null;
        LocalDateTime timeEnd = null;

        var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        formatter.withLocale(Locale.US);

        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            var errorOnRow = String.format("tại dòng %d không có dữ liệu.", i);
            var errorFormatOnRow = String.format("tại dòng %d không đúng định dạng.", i);
            Row row = sheet.getRow(i);
            if (i == 0) {
                continue;
            }
            // Validate required fields
            // Group category
            if (row.getCell(1) == null || row.getCell(2).getStringCellValue().isEmpty()) {
                return new GroupServiceDto(ReturnCodeConstants.GROUP_NOT_ENOUGH_FIELDS, String.format("Loại nhóm %s", errorOnRow), null);
            }
            groupCategoryName = row.getCell(1).getStringCellValue();
            if (!groupCategoryRepository.existsByName(groupCategoryName)) {
                return new GroupServiceDto(ReturnCodeConstants.GROUP_GROUP_CATEGORY_NOT_FOUND, "Group category not exists", groupCategoryName);
            }
            String groupCategoryId = groupCategoryRepository.findByName(groupCategoryName).getId();

            // Mentee email
            if (row.getCell(2) == null || row.getCell(2).getStringCellValue().isEmpty()) {
                return new GroupServiceDto(ReturnCodeConstants.GROUP_NOT_ENOUGH_FIELDS, String.format("Email người được quản lý %s", errorOnRow), null);
            }
            menteeEmails = Arrays.stream(row.getCell(2).getStringCellValue().split("\n"))
                    .filter(Objects::nonNull)
                    .filter(Predicate.not(String::isEmpty))
                    .toList();

            // Group name
            if (row.getCell(3) == null || row.getCell(3).getStringCellValue().isEmpty()) {
                return new GroupServiceDto(ReturnCodeConstants.GROUP_NOT_ENOUGH_FIELDS, String.format("Tên nhóm  %s", errorOnRow), null);
            }
            groupName = row.getCell(3).getStringCellValue();
            if (commands.containsKey(groupName) || groupCategoryRepository.existsByName(groupName))
                return new GroupServiceDto(ReturnCodeConstants.GROUP_DUPLICATE_GROUP, "Group name has been duplicated", groupName);

            // Mentor email
            if (row.getCell(5) == null || row.getCell(5).getStringCellValue().isEmpty()) {
                return new GroupServiceDto(ReturnCodeConstants.GROUP_NOT_ENOUGH_FIELDS, String.format("Email người quản lý %s", errorOnRow), null);
            }
            mentorEmails = Arrays.stream(row.getCell(5).getStringCellValue().split("\n"))
                    .filter(Objects::nonNull)
                    .filter(Predicate.not(String::isEmpty))
                    .toList();

            // Start date
            if (row.getCell(6) == null) {
                return new GroupServiceDto(ReturnCodeConstants.GROUP_NOT_ENOUGH_FIELDS, String.format("Ngày bắt đầu %s", errorOnRow), null);
            }
            var startDateCellType = row.getCell(6).getCellType();
            switch (startDateCellType) {
                case STRING:
                    var timeStartString = row.getCell(6).getStringCellValue();
                    try {
                        var ld = LocalDate.parse(timeStartString, formatter);
                        timeStart = LocalDateTime.of(ld, LocalTime.MIN);
                    } catch (DateTimeParseException e) {
                        return new GroupServiceDto(ReturnCodeConstants.GROUP_INVALID_TEMPLATE, String.format("Ngày bắt đầu %s %s, định dạng đúng phải là dd/MM/yyyy", timeStartString, errorFormatOnRow), null);
                    }

                    break;
                case NUMERIC:
                    timeStart = row.getCell(6).getLocalDateTimeCellValue();

                    break;
            }
            if (timeStart == null) {
                return new GroupServiceDto(ReturnCodeConstants.GROUP_NOT_ENOUGH_FIELDS, String.format("Ngày bắt đầu %s", errorOnRow), null);
            }

            // End date
            if (row.getCell(6) == null) {
                return new GroupServiceDto(ReturnCodeConstants.GROUP_NOT_ENOUGH_FIELDS, String.format("Ngày kết thúc %s", errorOnRow), null);
            }

            var endDateCellType = row.getCell(7).getCellType();
            switch (endDateCellType) {
                case STRING:
                    var timeEndString = row.getCell(7).getStringCellValue();
                    try {
                        var ld = LocalDate.parse(timeEndString, formatter);
                        timeEnd = LocalDateTime.of(ld, LocalTime.MIN);
                    } catch (DateTimeParseException e) {
                        return new GroupServiceDto(ReturnCodeConstants.GROUP_INVALID_TEMPLATE, String.format("Ngày kết thúc %s %s, định dạng đúng phải là dd/MM/yyyy", timeEndString, errorFormatOnRow), null);
                    }

                    break;
                case NUMERIC:
                    timeEnd = row.getCell(7).getLocalDateTimeCellValue();

                    break;
            }
            if (timeEnd == null) {
                return new GroupServiceDto(ReturnCodeConstants.GROUP_NOT_ENOUGH_FIELDS, String.format("Ngày kết thúc %s", errorOnRow), null);
            }

            var isValidTimeRange = groupDomainService.isStartAndEndTimeValid(timeStart, timeEnd);
            if (!isValidTimeRange) {
                return new GroupServiceDto(ReturnCodeConstants.GROUP_INVALID_DOMAINS, "Invalid time range", null);
            }

            var command = CreateGroupCommand.builder()
                    .name(groupName)
                    .description(description)
                    .createdDate(DateUtils.getDateNowAtUTC())
                    .menteeEmails(menteeEmails)
                    .mentorEmails(mentorEmails)
                    .groupCategory(groupCategoryId)
                    .timeStart(timeStart)
                    .timeEnd(timeEnd)
                    .build();
            commands.put(groupName, command);
        }

        return new GroupServiceDto(SUCCESS, "", commands.values().stream().toList());
    }

    private void removeBlankRows(Sheet sheet) {
        int lastRowNum = sheet.getLastRowNum();
        for (int i = lastRowNum; i >= 0; i--) {
            Row row = sheet.getRow(i);
            if (row == null
                    || row.getCell(0) == null
                    || row.getCell(0).getCellType() == org.apache.poi.ss.usermodel.CellType.BLANK) {
                sheet.removeRow(row);
            }
        }
    }
}

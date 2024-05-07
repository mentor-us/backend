package com.hcmus.mentor.backend.controller.usecase.group.importgroup;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.request.groups.CreateGroupRequest;
import com.hcmus.mentor.backend.controller.usecase.group.creategroup.CreateGroupCommand;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.repository.GroupCategoryRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.GroupService;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.service.ShareService;
import com.hcmus.mentor.backend.service.UserService;
import com.hcmus.mentor.backend.service.dto.GroupServiceDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;

import static com.hcmus.mentor.backend.controller.payload.returnCode.GroupReturnCode.*;
import static com.hcmus.mentor.backend.controller.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.controller.payload.returnCode.SuccessCode.SUCCESS;

/**
 * Handler for {@link ImportGroupCommand}
 */
@Component
@RequiredArgsConstructor
public class ImportGroupCommandHandler implements Command.Handler<ImportGroupCommand, GroupServiceDto> {
    private final GroupRepository groupRepository;
    private final PermissionService permissionService;
    private final GroupService groupService;
    private final ShareService shareService;
    private final GroupCategoryRepository groupCategoryRepository;
    private final UserRepository userRepository;
    private final Pipeline pipeline;
    private final UserService userService;

    /**
     * @param command command to import group
     * @return group service DTO
     */
    @Override
    public GroupServiceDto handle(final ImportGroupCommand command) {
        if (!permissionService.isAdmin(command.emailUser)) {
            return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        ReadFileGroupResult commands;
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
                return new GroupServiceDto(INVALID_TEMPLATE, "Invalid template", null);
            }

            GroupServiceDto validReadGroups = readGroups(workbook);
            if (!Objects.equals(validReadGroups.getReturnCode(), SUCCESS)) {
                return validReadGroups;
            }
            commands = (ReadFileGroupResult) validReadGroups.getData();
        } catch (ParseException | IOException e) {
            throw new DomainException(String.valueOf(e));
        }
        List<Group> groups = new ArrayList<>();
        for (var cm : commands.getCommands()) {
            GroupServiceDto returnData = pipeline.send(cm);
            if (!Objects.equals(returnData.getReturnCode(), SUCCESS)) {
                return returnData;
            }
            groups.add((Group) returnData.getData());
        }

        return new GroupServiceDto(SUCCESS, null, groups);
    }

    private GroupServiceDto readGroups(Workbook workbook) throws ParseException {
//        Map<String, Group> groups = new HashMap<>();
        Map<String, CreateGroupCommand> commands = new HashMap<>();
        Sheet sheet = workbook.getSheet("Data");
        removeBlankRows(sheet);
        String groupCategoryName;
        List<String> menteeEmails;
        List<String> mentorEmails;
        String groupName;
        String description = "";
        Date timeStart;
        Date timeEnd;

        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            var errorOnRow = String.format("tại dòng %d không có dữ liệu.", i);
            Row row = sheet.getRow(i);
            if (i == 0) {
                continue;
            }
            // Validate required fields
            // Group category
            if (row.getCell(1) == null || row.getCell(2).getStringCellValue().isEmpty()) {
                return new GroupServiceDto(NOT_ENOUGH_FIELDS, String.format("Loại nhóm %s", errorOnRow), null);
            }
            groupCategoryName = row.getCell(1).getStringCellValue();
            if (!groupCategoryRepository.existsByName(groupCategoryName)) {
                return new GroupServiceDto(GROUP_CATEGORY_NOT_FOUND, "Group category not exists", groupCategoryName);
            }
            String groupCategoryId = groupCategoryRepository.findByName(groupCategoryName).getId();

            // Mentee email
            if (row.getCell(2) == null || row.getCell(2).getStringCellValue().isEmpty()) {
                return new GroupServiceDto(NOT_ENOUGH_FIELDS, String.format("Email người được quản lý %s", errorOnRow), null);
            }
            menteeEmails = Arrays.stream(row.getCell(2).getStringCellValue().split("\n"))
                    .filter(Objects::nonNull)
                    .filter(Predicate.not(String::isEmpty))
                    .toList();

            // Group name
            if (row.getCell(3) == null || row.getCell(3).getStringCellValue().isEmpty()) {
                return new GroupServiceDto(NOT_ENOUGH_FIELDS, String.format("Tên nhóm  %s", errorOnRow), null);
            }
            groupName = row.getCell(3).getStringCellValue();
            if (commands.containsKey(groupName) || groupCategoryRepository.existsByName(groupName))
                return new GroupServiceDto(DUPLICATE_GROUP, "Group name has been duplicated", groupName);

            // Mentor email
            if (row.getCell(5) == null || row.getCell(5).getStringCellValue().isEmpty()) {
                return new GroupServiceDto(NOT_ENOUGH_FIELDS, String.format("Email người quản lý %s", errorOnRow), null);
            }
            mentorEmails = Arrays.stream(row.getCell(5).getStringCellValue().split("\n"))
                    .filter(Objects::nonNull)
                    .filter(Predicate.not(String::isEmpty))
                    .toList();

            // Start date
            if (row.getCell(6) == null || row.getCell(6).getDateCellValue() == null) {
                return new GroupServiceDto(NOT_ENOUGH_FIELDS, String.format("Ngày bắt đầu %s", errorOnRow), null);
            }
            timeStart = row.getCell(6).getDateCellValue();

            // End date
            if (row.getCell(7) == null || row.getCell(6).getDateCellValue() == null) {
                return new GroupServiceDto(NOT_ENOUGH_FIELDS, String.format("Ngày kết thúc %s", errorOnRow), null);
            }
            timeEnd = row.getCell(7).getDateCellValue();

            GroupServiceDto isValidTimeRange = groupService.validateTimeRange(timeStart, timeEnd);
            if (!Objects.equals(isValidTimeRange.getReturnCode(), SUCCESS)) {
                return isValidTimeRange;
            }

            var command = CreateGroupCommand.builder()
                    .request(CreateGroupRequest.builder()
                            .name(groupName)
                            .description(description)
                            .createdDate(new Date())
                            .menteeEmails(menteeEmails)
                            .mentorEmails(mentorEmails)
                            .groupCategory(groupCategoryId)
                            .timeStart(timeStart)
                            .timeEnd(timeEnd)
                            .build())
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

@Data
@AllArgsConstructor
class ReadFileGroupResult {
    private List<CreateGroupCommand> commands;
}

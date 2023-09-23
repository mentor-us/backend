package com.hcmus.mentor.backend.controller;

import com.google.api.services.drive.model.File;
import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.manager.GoogleDriveManager;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.StorageService;
import com.hcmus.mentor.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Tag(name = "File APIs", description = "APIs for file handling")
@Controller
public class FileController {

    private final static Logger LOGGER = LogManager.getLogger(FileController.class);

    private final StorageService storageService;

    private final UserService userService;

    private final GroupRepository groupRepository;

    private final GoogleDriveManager googleDriveManager;

    public FileController(StorageService storageService, UserService userService, GroupRepository groupRepository, GoogleDriveManager googleDriveManager) {
        this.storageService = storageService;
        this.userService = userService;
        this.groupRepository = groupRepository;
        this.googleDriveManager = googleDriveManager;
    }

    @Deprecated
    @Operation(summary = "Upload file import groups", description = "", tags = "File APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upload successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            )})
    @PostMapping("/import-group")
    public ResponseEntity<Group> uploadFile(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream);
        ) {
            Sheet sheet = workbook.getSheet("Data");
            Iterator<Row> rows = sheet.iterator();

            List<String> menteeIds = new ArrayList<>();
            List<String> mentorIds = new ArrayList<>();
            String groupName = "Group " + file.getOriginalFilename();
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }
                String email = currentRow.getCell(0).getStringCellValue();
                String userId = userService.getOrCreateUserByEmail(email, groupName);
                String type = currentRow.getCell(1).getStringCellValue();
                if (type.equals("0")) {
                    menteeIds.add(userId);
                } else {
                    mentorIds.add(userId);
                }
            };
            Group group = Group.builder()
                    .name(groupName)
                    .createdDate(new Date())
                    .mentees(menteeIds)
                    .mentors(mentorIds)
                    .build();
            return ResponseEntity.ok(groupRepository.save(group));
        } catch (Exception e) {
            LOGGER.error("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Download file", description = "Download file from Chat UI", tags = "File APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Download successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            )})
    @GetMapping("/api/files/{id}")
    public void downloadFile(@Parameter(hidden = true) @CurrentUser UserPrincipal user,
                             @PathVariable String id, HttpServletResponse response)
            throws IOException, GeneralSecurityException {
        File file = googleDriveManager.getFileById(id);
        response.addHeader("Content-disposition", "attachment; filename=" + file.getOriginalFilename());
        OutputStream output = googleDriveManager.downloadFile(id, response.getOutputStream());
        output.flush();
        output.close();
    }
}

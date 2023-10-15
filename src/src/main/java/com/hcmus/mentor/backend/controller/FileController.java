package com.hcmus.mentor.backend.controller;

import com.google.api.services.drive.model.File;
import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.infrastructure.fileupload.BlobStorage;
import com.hcmus.mentor.backend.infrastructure.fileupload.KeyGenerationStrategy;
import com.hcmus.mentor.backend.manager.GoogleDriveManager;
import com.hcmus.mentor.backend.payload.request.FileStorage.DeleteFileRequest;
import com.hcmus.mentor.backend.payload.request.FileStorage.DownloadFileReq;
import com.hcmus.mentor.backend.payload.request.FileStorage.ShareFileRequest;
import com.hcmus.mentor.backend.payload.response.file.DownloadFileResponse;
import com.hcmus.mentor.backend.payload.response.file.ShareFileResponse;
import com.hcmus.mentor.backend.payload.response.file.UploadFileResponse;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "File APIs", description = "APIs for file handling")
@Controller
@SecurityRequirement(name = "bearer")
@RequestMapping("/api/files")
public class FileController {

  private static final Logger logger = LogManager.getLogger(FileController.class);

  private final StorageService storageService;

  private final UserService userService;

  private final GroupRepository groupRepository;

  private final GoogleDriveManager googleDriveManager;

  private final BlobStorage blobStorage;

  private final KeyGenerationStrategy keyGenerationStrategy;
  private OutputStream outputStream;

  public FileController(
      StorageService storageService,
      UserService userService,
      GroupRepository groupRepository,
      GoogleDriveManager googleDriveManager,
      BlobStorage blobStorage,
      KeyGenerationStrategy keyGenerationStrategy) {
    this.storageService = storageService;
    this.userService = userService;
    this.groupRepository = groupRepository;
    this.googleDriveManager = googleDriveManager;
    this.blobStorage = blobStorage;
    this.keyGenerationStrategy = keyGenerationStrategy;
  }

  @Deprecated
  @Operation(summary = "Upload file import groups", description = "", tags = "File APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Upload successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class))))
  })
  @PostMapping("/import-group")
  public ResponseEntity<Group> uploadFile(@RequestParam("file") MultipartFile file) {
    try (InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream); ) {
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
      }
      ;
      Group group =
          Group.builder()
              .name(groupName)
              .createdDate(new Date())
              .mentees(menteeIds)
              .mentors(mentorIds)
              .build();
      return ResponseEntity.ok(groupRepository.save(group));
    } catch (Exception e) {
      logger.error("Error: " + e.getMessage());
      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(
      summary = "Download file",
      description = "Download file from Chat UI",
      tags = "File APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Download successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class))))
  })
  @GetMapping("/{id}")
  public void downloadFile(
      @Parameter(hidden = true) @CurrentUser UserPrincipal user,
      @PathVariable String id,
      HttpServletResponse response)
      throws IOException, GeneralSecurityException {
    File file = googleDriveManager.getFileById(id);
    response.addHeader("Content-disposition", "attachment; filename=" + file.getOriginalFilename());
    OutputStream output = googleDriveManager.downloadFile(id, response.getOutputStream());
    output.flush();
    output.close();
  }

  @Operation(summary = "Upload file using S3 SDK", description = "", tags = "File APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Upload file successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class))))
  })
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UploadFileResponse> uploadFileS3(@RequestPart("file") MultipartFile file) {

    try (InputStream inputStream = file.getInputStream(); ) {
      String contentType = file.getContentType();
      if (contentType == null) {
        contentType = "application/octet-stream";
      }
      String key =
          keyGenerationStrategy.generateBlobKey(
              contentType.substring(contentType.lastIndexOf("/") + 1));
      logger.info("Key: " + key);

      blobStorage.post(key, inputStream, file.getSize(), file.getContentType());

      UploadFileResponse response = new UploadFileResponse(key);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("Error: " + e.getMessage());

      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(summary = "Get file using S3 SDK", description = "", tags = "File APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get file successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class))))
  })
  @GetMapping(value = "/download")
  public ResponseEntity<InputStreamResource> downloadFile(DownloadFileReq request) {

    try {
      DownloadFileResponse response = blobStorage.get(request.getKey());

      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(response.getContentType()))
          .body(response.getStream());
    } catch (Exception e) {
      logger.error("Error: " + e.getMessage());

      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(summary = "Delete file from minio using S3 SDK", description = "", tags = "File APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Delete file successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class))))
  })
  @DeleteMapping(value = "/delete")
  public ResponseEntity<String> deleteFile(DeleteFileRequest request) {

    try {
      blobStorage.remove(request.getKey());

      return ResponseEntity.ok(String.format("File %s deleted successfully", request.getKey()));
    } catch (Exception e) {
      logger.error("Error: " + e.getMessage());

      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(
      summary = "Get URL of file from minio using S3 SDK",
      description = "",
      tags = "File APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get URL of file successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class))))
  })
  @GetMapping(value = "/share")
  public ResponseEntity<ShareFileResponse> shareFile(ShareFileRequest request) {
    try {
      String link = blobStorage.getLink(request.getKey());

      ShareFileResponse response = new ShareFileResponse(link);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("Error: " + e.getMessage());

      return ResponseEntity.internalServerError().build();
    }
  }
}

package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.infrastructure.fileupload.BlobStorage;
import com.hcmus.mentor.backend.payload.request.FileStorage.DeleteFileRequest;
import com.hcmus.mentor.backend.payload.request.FileStorage.DownloadFileReq;
import com.hcmus.mentor.backend.payload.request.FileStorage.ShareFileRequest;
import com.hcmus.mentor.backend.payload.response.file.ShareFileResponse;
import com.hcmus.mentor.backend.payload.response.file.UploadFileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "File APIs", description = "APIs for file handling")
@Controller
@SecurityRequirement(name = "bearer")
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

  private static final Logger logger = LogManager.getLogger(FileController.class);

  private final BlobStorage blobStorage;

  /**
   * Get file from server.
   *
   * @param request Download file request.
   * @return File content.
   * @throws IOException Exception.
   */
  @Operation(summary = "Get file from server", tags = "File APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get file successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class))))
  })
  @GetMapping("")
  public ResponseEntity<Resource> getFile(@ParameterObject DownloadFileReq request)
      throws IOException {
    java.io.File file = blobStorage.get(request.getKey());

    Resource resource = new FileSystemResource(file);

    return ResponseEntity.ok()
        .contentType(MediaType.valueOf(new Tika().detect(request.getKey())))
        .body(resource);
  }

  @Operation(summary = "Upload file to server", tags = "File APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Upload file successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class))))
  })
  @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UploadFileResponse> uploadFile(
      @RequestParam("file") MultipartFile multipartFile) throws IOException {
    // convert multipart file  to a file
    File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
      fileOutputStream.write(multipartFile.getBytes());
    }
    String mimeType = new Tika().detect(file);

    String key = blobStorage.generateBlobKey(mimeType);

    blobStorage.post(key, file);
    UploadFileResponse response = new UploadFileResponse(key);

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Remove file from server", tags = "File APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Delete file successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class))))
  })
  @DeleteMapping(value = "")
  public ResponseEntity<String> removeFile(@ParameterObject DeleteFileRequest request) {
    try {
      blobStorage.remove(request.getKey());

      return ResponseEntity.ok(String.format("File %s deleted successfully", request.getKey()));
    } catch (Exception e) {
      logger.error("Error: " + e.getMessage());

      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(summary = "Get direct url to file in server", tags = "File APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get URL of file successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class))))
  })
  @GetMapping(value = "/url")
  public ResponseEntity<ShareFileResponse> getFileUrl(@ParameterObject ShareFileRequest request) {
    String link = blobStorage.getUrl(request.getKey());

    ShareFileResponse response = new ShareFileResponse(link);

    return ResponseEntity.ok(response);
  }
}

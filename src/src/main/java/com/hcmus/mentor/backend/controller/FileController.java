package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.payload.request.FileStorage.DeleteFileRequest;
import com.hcmus.mentor.backend.controller.payload.request.FileStorage.DownloadFileReq;
import com.hcmus.mentor.backend.controller.payload.request.FileStorage.ShareFileRequest;
import com.hcmus.mentor.backend.controller.payload.response.file.ShareFileResponse;
import com.hcmus.mentor.backend.controller.payload.response.file.UploadFileResponse;
import com.hcmus.mentor.backend.service.fileupload.BlobStorage;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "files")
@RestController
@RequestMapping("api/files")
@RequiredArgsConstructor
@Validated
public class FileController {

    private static final Logger logger = LogManager.getLogger(FileController.class);
    private final BlobStorage blobStorage;

    /**
     * Retrieves a file from the server.
     *
     * @param request Contains the details for the file download request.
     * @return A ResponseEntity containing the InputStreamResource of the file.
     */
    @Cacheable("controller_getFile")
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<Resource> getFile(@ParameterObject DownloadFileReq request) {
        try{
            var stream = blobStorage.get(request.getKey());
            var contentType = new Tika().detect(request.getKey());

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(contentType))
                .body(new InputStreamResource(stream));
        }
        catch (Exception e) {
            logger.error(e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Uploads a file to the server.
     *
     * @param file The file to be uploaded.
     * @return A ResponseEntity containing the UploadFileResponse.
     */
    @SecurityRequirement(name = "bearer")
    @SneakyThrows
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<UploadFileResponse> uploadFile(@RequestPart MultipartFile file) {
        String key = blobStorage.generateBlobKey(new Tika().detect(file.getBytes()));

        blobStorage.post(file, key);
        UploadFileResponse response = new UploadFileResponse(key);

        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a file from the server.
     *
     * @param request Contains the details for the file deletion request.
     * @return A ResponseEntity containing a success message if deletion is successful.
     */
    @SecurityRequirement(name = "bearer")
    @DeleteMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<String> removeFile(@ParameterObject DeleteFileRequest request) {
        try {
            blobStorage.remove(request.getKey());

            return ResponseEntity.ok(String.format("File %s deleted successfully", request.getKey()));
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());

            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves a sharable URL for a file.
     *
     * @param request Contains the details for the file sharing request.
     * @return A ResponseEntity containing the ShareFileResponse with the file URL.
     */
    @SecurityRequirement(name = "bearer")
    @GetMapping("url")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<ShareFileResponse> getFileUrl(@ParameterObject ShareFileRequest request) {
        try {
            String link = blobStorage.getUrl(request.getKey());

            ShareFileResponse response = new ShareFileResponse(link);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());

            return ResponseEntity.internalServerError().build();
        }

    }
}
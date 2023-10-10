package com.hcmus.mentor.backend.manager;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.hcmus.mentor.backend.config.GoogleDriveConfig;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class GoogleDriveManager {

  public static final String ROOT_ID = "1-7y4Ex3mt-EgqHAMkLa1o0McFGitIOKH";
  private static final Logger LOGGER = LogManager.getLogger(GoogleDriveManager.class);
  private final GoogleDriveConfig googleDriveConfig;

  public GoogleDriveManager(GoogleDriveConfig googleDriveConfig) {
    this.googleDriveConfig = googleDriveConfig;
  }

  public String getFolderIdByName(String parentId, String folderName)
      throws GeneralSecurityException, IOException {
    Drive service = googleDriveConfig.getInstance();

    File fileMetadata = new File();
    fileMetadata.setMimeType("application/vnd.google-apps.folder");
    fileMetadata.setName(folderName);

    String folderId = null;
    String pageToken = null;
    FileList result = null;
    do {
      String query = " mimeType = 'application/vnd.google-apps.folder' ";
      if (parentId == null) {
        query = query + " and 'root' in parents";
      } else {
        query = query + " and '" + parentId + "' in parents";
      }
      result =
          service
              .files()
              .list()
              .setQ(query)
              .setSpaces("drive")
              .setFields("nextPageToken, files(id, name)")
              .setPageToken(pageToken)
              .execute();
      for (File file : result.getFiles()) {
        if (file.getName().equalsIgnoreCase(folderName)) {
          folderId = file.getId();
        }
      }
      pageToken = result.getNextPageToken();
    } while (pageToken != null && folderId == null);

    return folderId;
  }

  public String findOrCreateFolder(String parentId, String folderName)
      throws GeneralSecurityException, IOException {
    Drive service = googleDriveConfig.getInstance();

    String folderId = getFolderIdByName(parentId, folderName);
    if (folderId != null) {
      return folderId;
    }

    File fileMetadata = new File();
    fileMetadata.setMimeType("application/vnd.google-apps.folder");
    fileMetadata.setName(folderName);
    if (parentId != null) {
      fileMetadata.setParents(Collections.singletonList(parentId));
    }

    return service.files().create(fileMetadata).setFields("id").execute().getId();
  }

  public File uploadToFolder(String folderName, MultipartFile file)
      throws GeneralSecurityException, IOException {
    String folderId = findOrCreateFolder(ROOT_ID, folderName);
    Drive service = googleDriveConfig.getInstance();
    File fileMetadata = new File();
    fileMetadata.setName(file.getOriginalFilename());
    fileMetadata.setParents(Collections.singletonList(folderId));
    File uploadedFile =
        service
            .files()
            .create(
                fileMetadata,
                new InputStreamContent(
                    file.getContentType(), new ByteArrayInputStream(file.getBytes())))
            .setFields("id, parents")
            .execute();
    LOGGER.info("[*] Uploaded file: " + uploadedFile.getId() + " in " + uploadedFile.getParents());
    return uploadedFile;
  }

  public OutputStream downloadFile(String id, OutputStream outputStream)
      throws GeneralSecurityException, IOException {
    Drive service = googleDriveConfig.getInstance();
    if (id == null) {
      return outputStream;
    }
    service.files().get(id).executeMediaAndDownloadTo(outputStream);
    return outputStream;
  }

  public void deleteFile(String fileId) throws GeneralSecurityException, IOException {
    Drive service = googleDriveConfig.getInstance();
    service.files().delete(fileId).execute();
  }

  public List<File> listFolderContent(String parentId)
      throws IOException, GeneralSecurityException {
    if (parentId == null) {
      parentId = "root";
    }
    String query = "'" + parentId + "' in parents";
    FileList result =
        googleDriveConfig
            .getInstance()
            .files()
            .list()
            .setQ(query)
            .setPageSize(10)
            .setFields("nextPageToken, files(id, name)")
            .execute();
    return result.getFiles();
  }

  public File getFileById(String fileId) throws IOException, GeneralSecurityException {
    return googleDriveConfig.getInstance().files().get(fileId).execute();
  }
}

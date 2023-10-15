package com.hcmus.mentor.backend.infrastructure.fileupload;

import com.hcmus.mentor.backend.payload.response.file.DownloadFileResponse;
import java.io.IOException;
import java.io.InputStream;

public interface BlobStorage {
  String bucket = null;

  void post(String key, InputStream stream, Long contentLength, String contentType);

  DownloadFileResponse get(String key) throws IOException;

  String getLink(String key);

  void remove(String key);
}

package com.hcmus.mentor.backend.infrastructure.fileupload;

import java.io.File;
import java.io.IOException;

/** Blob storage service. */
public interface BlobStorage {

  File get(String key) throws IOException;

  void remove(String key);

  void post(String key, File file);

  String generateBlobKey(String mimeType);

  Boolean exists(String key);

  String getUrl(String key);
}

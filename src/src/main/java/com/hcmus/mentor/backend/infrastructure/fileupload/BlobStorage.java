package com.hcmus.mentor.backend.infrastructure.fileupload;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/** Blob storage service. */
public interface BlobStorage {

  File get(String key) throws IOException;

  void remove(String key);

  void post(String key, File file);

  String generateBlobKey(String mimeType);

  Boolean exists(String key);

  String getUrl(String key)
      throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
}

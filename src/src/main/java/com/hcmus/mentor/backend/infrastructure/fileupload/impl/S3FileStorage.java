package com.hcmus.mentor.backend.infrastructure.fileupload.impl;

import com.hcmus.mentor.backend.infrastructure.fileupload.BlobStorage;
import com.hcmus.mentor.backend.infrastructure.fileupload.KeyGenerationStrategy;
import com.hcmus.mentor.backend.infrastructure.fileupload.S3Settings;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * {@inheritDoc}
 */
@Service
public class S3FileStorage implements BlobStorage {
//  private final S3Client s3Client;
//  private final S3Settings s3Settings;

  private final MinioClient minioClient;
  private final KeyGenerationStrategy keyGenerationStrategy;
  private final Logger logger = LogManager.getLogger(this.getClass());

  /**
   * Constructor.
   *
   * @param s3Settings            S3Settings
   * @param keyGenerationStrategy Key generation strategy.
   */
  public S3FileStorage(S3Settings s3Settings,
      KeyGenerationStrategy keyGenerationStrategy) {
    this.keyGenerationStrategy = keyGenerationStrategy;


    this.minioClient = MinioClient.builder()
        .endpoint(s3Settings.ServiceUrl)
        .credentials(s3Settings.AccessKey, s3Settings.SecretKey)
        .build();

    this.bucket = s3Settings.BucketName;
  }

  private final String bucket;

  /**
   * {@inheritDoc}
   */
  public File get(String key)
      throws IOException,
      ServerException,
      InternalException,
      XmlParserException,
      InvalidKeyException,
      ErrorResponseException,
      InvalidResponseException,
      NoSuchAlgorithmException,
      InsufficientDataException {
    try (InputStream stream = minioClient.getObject(
        GetObjectArgs.builder()
            .bucket(bucket)
            .object(key)
            .build())) {

      File file = new File(key);
      FileUtils.copyInputStreamToFile(stream, file);

      logger.log(Level.INFO, "File {} is received successfully.", key);
      return file;
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(String key)
      throws
      IOException,
      ServerException,
      InternalException,
      XmlParserException,
      InvalidKeyException,
      ErrorResponseException,
      NoSuchAlgorithmException,
      InvalidResponseException,
      InsufficientDataException {
    minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());

    logger.log(Level.INFO, "File '{}' is removed successfully.", key);
  }


  @Override
  public void post(MultipartFile file, String key)
      throws
      IOException,
      ServerException,
      InternalException,
      XmlParserException,
      InvalidKeyException,
      ErrorResponseException,
      InvalidResponseException,
      NoSuchAlgorithmException,
      InsufficientDataException {
    minioClient.putObject(
        PutObjectArgs.builder()
            .bucket(bucket)
            .object(key)
            .contentType(file.getContentType())
            .stream(file.getInputStream(), file.getSize(), -1)
            .build());
    logger.log(Level.INFO, "File {} is uploaded successfully", key);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String generateBlobKey(String mimeType) {
    return keyGenerationStrategy.generateBlobKey(mimeType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Boolean exists(String key)
      throws
      IOException,
      ServerException,
      InternalException,
      XmlParserException,
      InvalidKeyException,
      ErrorResponseException,
      InvalidResponseException,
      NoSuchAlgorithmException,
      InsufficientDataException {
    minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build());

    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getUrl(String key)
      throws
      IOException,
      ServerException,
      InternalException,
      XmlParserException,
      InvalidKeyException,
      ErrorResponseException,
      InvalidResponseException,
      NoSuchAlgorithmException,
      InsufficientDataException {

    GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
        .method(Method.GET)
        .bucket(bucket)
        .object(key)
        .build();

    return minioClient.getPresignedObjectUrl(args);
  }
}

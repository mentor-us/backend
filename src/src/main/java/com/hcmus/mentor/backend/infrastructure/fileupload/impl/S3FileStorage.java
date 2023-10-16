package com.hcmus.mentor.backend.infrastructure.fileupload.impl;

import com.hcmus.mentor.backend.infrastructure.fileupload.BlobStorage;
import com.hcmus.mentor.backend.infrastructure.fileupload.KeyGenerationStrategy;
import com.hcmus.mentor.backend.infrastructure.fileupload.S3Settings;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/** {@inheritDoc} */
@Service
public class S3FileStorage implements BlobStorage {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final S3Settings s3Settings;
  private final KeyGenerationStrategy keyGenerationStrategy;
  private final Logger logger = LogManager.getLogger(this.getClass());

  /**
   * Constructor.
   *
   * @param s3Settings S3Settings
   * @param keyGenerationStrategy Key generation strategy.
   */
  public S3FileStorage(S3Settings s3Settings, KeyGenerationStrategy keyGenerationStrategy) {
    this.s3Settings = s3Settings;
    this.keyGenerationStrategy = keyGenerationStrategy;

    AwsCredentialsProvider credentials =
        StaticCredentialsProvider.create(
            AwsBasicCredentials.create(s3Settings.AccessKey, s3Settings.SecretKey));

    this.s3Client =
        S3Client.builder()
            .credentialsProvider(credentials)
            .region(Region.of(s3Settings.RegionName))
            .endpointOverride(URI.create(s3Settings.ServiceUrl))
            .forcePathStyle(s3Settings.ForcePathStyle)
            .build();
    this.s3Presigner =
        S3Presigner.builder()
            .credentialsProvider(credentials)
            .region(Region.of(s3Settings.RegionName))
            .endpointOverride(URI.create(s3Settings.ServiceUrl))
            .build();

    this.bucket = s3Settings.BucketName;
  }

  private final String bucket;

  /** {@inheritDoc} */
  public File get(String key) throws IOException {
    var request = GetObjectRequest.builder().bucket(bucket).key(key).build();
    ResponseInputStream<GetObjectResponse> object = s3Client.getObject(request);

    File file = new File(key);
    FileUtils.copyInputStreamToFile(object, file);

    logger.log(Level.INFO, "File {} is received successfully.", key);

    return file;
  }

  /** {@inheritDoc} */
  @Override
  public void remove(String key) {
    if (exists(key)) {
      var request = DeleteObjectRequest.builder().bucket(bucket).key(key).build();

      s3Client.deleteObject(request);
    }

    logger.log(Level.INFO, "File '{}' is removed successfully.", key);
  }

  /** {@inheritDoc} */
  @Override
  public void post(String key, File file) {
    var request = PutObjectRequest.builder().bucket(bucket).key(key).build();

    s3Client.putObject(request, RequestBody.fromFile(file));

    logger.log(Level.INFO, "File {} is uploaded successfully", key);
  }

  /** {@inheritDoc} */
  @Override
  public String generateBlobKey(String mimeType) {
    return keyGenerationStrategy.generateBlobKey(mimeType);
  }

  /** {@inheritDoc} */
  @Override
  public Boolean exists(String key) {
    var request = HeadObjectRequest.builder().bucket(bucket).key(key).build();

    var response = s3Client.headObject(request);

    return response != null;
  }

  /** {@inheritDoc} */
  @Override
  public String getUrl(String key) {
    /*
    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();

    GetObjectPresignRequest getObjectPresignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(60))
            .getObjectRequest(getObjectRequest)
            .build();

    PresignedGetObjectRequest presignedGetObjectRequest =
        s3Presigner.presignGetObject(getObjectPresignRequest);

    return presignedGetObjectRequest.url().toString();
    */

    return String.format("%s%s/%s", s3Settings.ServiceUrl, bucket, key);
  }
}

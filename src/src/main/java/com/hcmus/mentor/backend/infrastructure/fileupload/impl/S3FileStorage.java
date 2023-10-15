package com.hcmus.mentor.backend.infrastructure.fileupload.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.hcmus.mentor.backend.infrastructure.fileupload.BlobStorage;
import com.hcmus.mentor.backend.infrastructure.fileupload.S3Settings;
import com.hcmus.mentor.backend.payload.response.file.DownloadFileResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/** Store file in minio with s3 */
@Service
public class S3FileStorage implements BlobStorage {

  private final AmazonS3 amazonS3;
  private final S3Client s3Client;
  private final S3Settings s3Settings;
  private final String bucket;
  private final Logger logger = LogManager.getLogger(this.getClass());

  /**
   * Constructor
   *
   * @param amazonS3 AmazonS3
   * @param s3Settings S3Settings
   */
  public S3FileStorage(AmazonS3 amazonS3, S3Settings s3Settings) {
    this.amazonS3 = amazonS3;
    this.s3Settings = s3Settings;

    this.bucket = s3Settings.BucketName;

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
  }

  //
  //  /**
  //   * @param key
  //   * @param stream
  //   * @param contentType
  //   * @return
  //   */
  //  @Override
  //  public String post(String key, Stream stream, String contentType) {
  //        TransferManager transferManagerUtils = new TransferManager();
  //        ObjectMetadata metadata = new ObjectMetadata();
  //        metadata.setContentType(contentType);
  //        PutObjectRequest request = new PutObjectRequest("image", key, (InputStream) stream,
  //     metadata);
  //        transferManagerUtils.upload(request);
  //
  //        return key;
  //  }

  /**
   * @param key File key
   * @param stream File stream
   * @param contentLength File size
   * @param contentType File type
   */
  @Override
  public void post(String key, InputStream stream, Long contentLength, String contentType) {
    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .contentLength(contentLength)
            .build(),
        RequestBody.fromInputStream(stream, contentLength));
  }

  /**
   * Get file from S3
   *
   * @param key File key
   * @return OutputStream
   */
  public DownloadFileResponse get(String key) throws IOException {
    ResponseBytes<GetObjectResponse> objectBytes =
        s3Client.getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(key).build());

    DownloadFileResponse response =
        new DownloadFileResponse(
            new InputStreamResource(objectBytes.asInputStream()),
            objectBytes.response().contentType());

    return response;
  }

  /**
   * @param key File key
   * @return String
   */
  @Override
  public String getLink(String key) {

    //    AwsCredentialsProvider credentials =
    //        StaticCredentialsProvider.create(
    //            AwsBasicCredentials.create(s3Settings.AccessKey, s3Settings.SecretKey));
    //
    //    S3Presigner presigner =
    //        S3Presigner.builder()
    //            .credentialsProvider(credentials)
    //            .region(Region.of(s3Settings.RegionName))
    //            .build();
    //
    //    GetObjectRequest getObjectRequest =
    // GetObjectRequest.builder().bucket(bucket).key(key).build();
    //
    //    GetObjectPresignRequest getObjectPresignRequest =
    //        GetObjectPresignRequest.builder()
    //            .signatureDuration(Duration.ofMinutes(10))
    //            .getObjectRequest(getObjectRequest)
    //            .build();
    //
    //    PresignedGetObjectRequest presignedGetObjectRequest =
    //        presigner.presignGetObject(getObjectPresignRequest);

    //    logger.info("Presigned URL: " + presignedGetObjectRequest.url());

    //    return presignedGetObjectRequest.url().toString();

    //    ResponseInputStream<GetObjectResponse> object =
    //        s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
    //    s3Client.getBucketAcl(builder -> builder.bucket(bucket).build());
    //    String url = object.response().websiteRedirectLocation();
    //    java.util.Date expiration = new java.util.Date();
    //    long expTimeMillis = expiration.getTime();
    //    expTimeMillis += 604800 * 1000; // 7 days
    //    expiration.setTime(expTimeMillis);
    //    url = s3Client.generatePresignedUrl(bucket, key, expiration, HttpMethod.GET);
    String url =
        amazonS3.generatePresignedUrl(new GeneratePresignedUrlRequest(bucket, key)).toString();

    return url;
  }

  /**
   * Delete file from S3
   *
   * @param key File key
   */
  @Override
  public void remove(String key) {
    s3Client.deleteObject(builder -> builder.bucket(bucket).key(key).build());
  }
}

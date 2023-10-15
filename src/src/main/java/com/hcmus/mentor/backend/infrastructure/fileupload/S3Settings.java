package com.hcmus.mentor.backend.infrastructure.fileupload;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

/** */
@Getter
public class S3Settings {

  /** */
  @Value("${s3-settings.access-key}")
  public String AccessKey;

  /** */
  @Value("${s3-settings.secret-key}")
  public String SecretKey;

  /** */
  @Value("${s3-settings.region-name}")
  public String RegionName;

  /** */
  @Value("${s3-settings.bucket-name}")
  public String BucketName;

  /** */
  @Value("${s3-settings.service-url}")
  public String ServiceUrl;

  /** */
  @Value("${s3-settings.force-path-style:#{false}}")
  public Boolean ForcePathStyle;
}

package com.hcmus.mentor.backend.infrastructure.fileupload;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Getter
@Configuration
public class AmazonS3Config {
  @Value("${s3-settings.service-url}")
  private String endPoint;

  @Value("${s3-settings.access-key}")
  private String accessKey;

  @Value("${s3-settings.secret-key}")
  private String secretKey;

  @Value("${s3-settings.bucket-name}")
  private String bucketName;

  @Bean
  public AmazonS3 generateAmazonS3Config() {
    try {
      ClientConfiguration clientConfig = new ClientConfiguration();
      clientConfig.setProtocol(Protocol.HTTP);
      AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

      AmazonS3 s3client =
          AmazonS3ClientBuilder.standard()
              .withEndpointConfiguration(
                  new AwsClientBuilder.EndpointConfiguration(endPoint, Regions.US_EAST_1.name()))
              .withPathStyleAccessEnabled(true)
              .withClientConfiguration(clientConfig)
              .withCredentials(new AWSStaticCredentialsProvider(credentials))
              .build();

      return s3client;
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}

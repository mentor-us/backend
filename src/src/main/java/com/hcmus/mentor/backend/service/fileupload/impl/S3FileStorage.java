package com.hcmus.mentor.backend.service.fileupload.impl;

import com.google.common.collect.Lists;
import com.hcmus.mentor.backend.service.fileupload.BlobStorage;
import com.hcmus.mentor.backend.service.fileupload.KeyGenerationStrategy;
import com.hcmus.mentor.backend.service.fileupload.S3Settings;
import io.minio.*;
import io.minio.http.Method;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * {@inheritDoc}
 */
@Service
public class S3FileStorage implements BlobStorage {

    private final Logger logger = LogManager.getLogger(S3FileStorage.class);
    private final MinioClient minioClient;
    private final KeyGenerationStrategy keyGenerationStrategy;
    private final String bucket;

    /**
     * Constructor.
     *
     * @param s3Settings            S3Settings
     * @param keyGenerationStrategy Key generation strategy.
     */
    public S3FileStorage(
            S3Settings s3Settings,
            KeyGenerationStrategy keyGenerationStrategy) {
        this.keyGenerationStrategy = keyGenerationStrategy;

        this.minioClient = MinioClient.builder()
                .endpoint(s3Settings.ServiceUrl)
                .credentials(s3Settings.AccessKey, s3Settings.SecretKey)
                .build();

        this.bucket = s3Settings.BucketName;
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public InputStream get(String key) {
        var getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .object(key)
                .build();

        try (InputStream stream = minioClient.getObject(getObjectArgs)) {
            return new ByteArrayInputStream(stream.readAllBytes());
        }
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public void remove(String key) {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());

        logger.info("File '{}' is removed successfully.", key);
    }


    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public void post(MultipartFile file, String key) {
        var tika = new Tika();
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(key)
                .contentType(tika.detect(file.getInputStream()))
                .stream(file.getInputStream(), file.getSize(), -1)
                .build());

        logger.info("File '{}' is uploaded successfully", key);
    }

    @SneakyThrows
    @Override
    public void post(byte[] bytes, String key) {
        var stream = new ByteArrayInputStream(bytes);

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(key)
                .contentType(new Tika().detect(bytes))
                .stream(stream, stream.available(), -1)
                .build());

        logger.info("File '{}' is uploaded successfully", key);
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
    @SneakyThrows
    @Override
    public Boolean exists(String key) {
        minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build());

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public String getUrl(String key) {
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .object(key)
                .build();

        return minioClient.getPresignedObjectUrl(args);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @SneakyThrows
    @Override
    public String copyFile(String sourceKey) {
        String newKey = generateNewKey(sourceKey);
        minioClient.copyObject(CopyObjectArgs.builder()
                .source(CopySource.builder().bucket(bucket).object(sourceKey).build())
                .bucket(bucket)
                .object(newKey)
                .build());

        return newKey;
    }

    private String generateNewKey(String oldKey) {
        var oldType = Lists.reverse(Arrays.asList(oldKey.split("\\."))).stream().findFirst().orElse("");
        return generateBlobKey(oldType);
    }
}

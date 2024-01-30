package com.hcmus.mentor.backend.service.fileupload;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3SettingsConfig {

    @Bean
    public S3Settings GetS3Settings() {

        return new S3Settings();
    }
}

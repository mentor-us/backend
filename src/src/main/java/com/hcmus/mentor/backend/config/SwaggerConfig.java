package com.hcmus.mentor.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info =
        @Info(
                title = "MentorUS Backend",
                description = "BE API specification for web dashboard and mobile app",
                version = "0.0.1"))
@SecurityScheme(
        description = "Insert JWT token to the field.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        name = "bearer",
        bearerFormat = "JWT")
public class SwaggerConfig {
}

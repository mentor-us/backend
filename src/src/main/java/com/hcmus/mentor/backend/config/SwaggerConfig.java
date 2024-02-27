package com.hcmus.mentor.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${app.version}")
    private String version;

    @Bean
    public OpenAPI mentorUSOpenAPI() {
        var info = new Info()
                .title("MentorUS Backend")
                .description("BE API specification for web dashboard and mobile app")
                .contact(new io.swagger.v3.oas.models.info.Contact().name("MentorUS Team").email("mentoring@fit.hcmus.edu.vn"))
                .version(version);

        var bearerSchemaKey = "bearer";
        var securityComponent = new Components()
                .addSecuritySchemes(bearerSchemaKey, new SecurityScheme()
                        .name(bearerSchemaKey)
                        .description("Insert JWT token to the field.")
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .type(SecurityScheme.Type.HTTP));

        return new OpenAPI().info(info).components(securityComponent);
    }
}

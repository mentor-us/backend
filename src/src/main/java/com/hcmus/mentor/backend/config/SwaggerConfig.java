package com.hcmus.mentor.backend.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableSwagger2
public class SwaggerConfig {

    /*
        @Bean
        public Docket api() {
            return new Docket(DocumentationType.SWAGGER_2)
                    .select()
                    .apis(RequestHandlerSelectors.basePackage("com.hcmus.mentor.backend.controller"))
                    .paths(PathSelectors.any())
                    .build()
                    .apiInfo(metaData());
        }

        private ApiInfo metaData() {
            return new ApiInfoBuilder()
                    .title("MentorUS BE")
                    .description("BE API specification for web dashboard and mobile app")
                    .version("1.0")
                    .termsOfServiceUrl("Terms of service")
                    .license("No license")
                    .licenseUrl("No")
                    .build();
        }
    */

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI().info(new Info().title("SpringShop API").description("Spring shop sample application").version("v0.0.1").license(new License().name("Apache 2.0").url("http://springdoc.org"))).externalDocs(new ExternalDocumentation().description("SpringShop Wiki Documentation").url("https://springshop.wiki.github.org/docs"));
    }
}

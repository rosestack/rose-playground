package com.company.todo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI todoOpenAPI() {
        return new OpenAPI().info(new Info().title("Todo API").version("v1").description("Todo service endpoints"));
    }
}

package com.oath.common.config;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(
                info = @Info(title = "Oathkeeper API", version = "v1.0",
                                description = "약속지킴이 Oathkeeper 프로젝트 API 명세서"),
                security = {@SecurityRequirement(name = "Bearer Authentication")})
@SecurityScheme(name = "Bearer Authentication", type = SecuritySchemeType.HTTP,
                bearerFormat = "JWT", scheme = "bearer")
public class SwaggerConfig {
}

package com.example.petlog.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    // application.yml의 openapi.service.url 값을 가져옴
    @Value("${openapi.service.url}")
    private String gatewayUrl;

    @Bean
    public OpenAPI openAPI() {
        // 1. 보안 스키마 설정 (JWT 토큰 인증 버튼 추가)
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("BearerAuth");

        // 2. 서버 설정 (Gateway 주소로 요청을 보내도록 설정)
        Server server = new Server()
                .url(gatewayUrl)
                .description("API Gateway Server");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("BearerAuth", securityScheme))
                .security(List.of(securityRequirement))
                .servers(List.of(server)) // Gateway URL 등록
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Social Service API")
                .description("PetLog 소셜(피드) 서비스 API 명세서")
                .version("1.0.0");
    }
}
package com.petlog.social.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList; // ✅ ArrayList 임포트 필수
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${openapi.service.url}")
    private String gatewayUrl;

    @Bean
    public OpenAPI openAPI() {
        // 1. 보안 스키마 설정 (JWT 토큰 인증)
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("BearerAuth");

        // 2. 서버 설정 (Gateway 주소)
        Server server = new Server()
                .url(gatewayUrl)
                .description("API Gateway Server");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("BearerAuth", securityScheme))
                // ✅ [수정] List.of()를 new ArrayList<>()로 감싸서 수정 가능한 리스트로 변경
                .security(new ArrayList<>(List.of(securityRequirement)))
                // ✅ [수정] 마찬가지로 servers 리스트도 수정 가능하도록 변경
                .servers(new ArrayList<>(List.of(server)))
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Social Service API")
                .description("PetLog 소셜(피드) 서비스 API 명세서")
                .version("1.0.0");
    }
}
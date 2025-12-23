package com.petlog.social.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 비활성화 (REST API는 보통 비활성화)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.disable())

                // 2. HTTP Basic 로그인 비활성화 (로그인창 안 뜨게 함)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 3. Form 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // 4. 모든 요청 허용 (일단 개발을 위해)
                .authorizeHttpRequests(auth -> auth

                       // 헬스체크 (ALB / K8s 필수)
                       .requestMatchers(
                                "/actuator/health",
                               "/actuator/health/**",
                               "/api/actuator/health",
                               "/api/actuator/health/**"
                       ).permitAll()

                        // Swagger 관련 경로 모두 허용 (로그인 없이 접속 가능)
                        .requestMatchers(
                                "/swagger",
                                "/swagger/",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 개발 초기 : 아래처럼 다 열어두고 시작
                         //.anyRequest().authenticated() // (개발 후: 나머지는 인증 필요)
                        .anyRequest().permitAll()      // (개발 편의상: 일단 다 허용)
                );

        return http.build();
    }
}
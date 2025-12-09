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

                // 2. HTTP Basic 로그인 비활성화 (로그인창 안 뜨게 함)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 3. Form 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // 4. 모든 요청 허용 (일단 개발을 위해)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
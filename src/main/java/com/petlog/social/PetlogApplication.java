package com.petlog.social;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients // Feign Client 사용 활성화
@EnableJpaAuditing
public class PetlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetlogApplication.class, args);
    }

    // [추가] 배포 서버 시간대를 한국 시간(KST)으로 설정
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
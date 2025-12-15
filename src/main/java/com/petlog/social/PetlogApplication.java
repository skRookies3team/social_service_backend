package com.petlog.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableFeignClients // Feign Client 사용 활성화
@EnableJpaAuditing
public class PetlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetlogApplication.class, args);
    }

}
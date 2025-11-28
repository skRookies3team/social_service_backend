package com.example.petlog.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// User 서비스의 이름이 'user-service'라고 가정
@FeignClient(name = "user-service", url = "${external.user-service.url}")
public interface UserClient {
    @GetMapping("/api/users/{userId}/nickname")
    String getNickname(@PathVariable("userId") Long userId);

    // 검증용 메서드 (필요시)
    @GetMapping("/api/users/{userId}/exists")
    boolean checkUserExists(@PathVariable("userId") Long userId);
}
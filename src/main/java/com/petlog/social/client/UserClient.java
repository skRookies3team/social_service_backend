package com.petlog.social.client;

import com.petlog.social.dto.client.UserClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${external.user-service.url}")
public interface UserClient {
    // [수정] 닉네임만 가져오는 API가 없으므로, 유저 전체 정보를 조회하는 API로 변경
    @GetMapping("/api/users/{userId}")
    UserClientResponse getUser(@PathVariable("userId") Long userId);
}
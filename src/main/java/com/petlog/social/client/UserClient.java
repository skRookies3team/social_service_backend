package com.petlog.social.client;

import com.petlog.social.dto.client.UserClientResponse;
import com.petlog.social.dto.client.UserSearchListResponse; // [New]
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "${external.user-service.url}")
public interface UserClient {

    // 기존 단건 조회
    @GetMapping("/api/users/{userId}")
    UserClientResponse getUser(@PathVariable("userId") Long userId);

    // [New] 유저 검색 (반환 타입 변경)
    @GetMapping("/api/users/search")
    UserSearchListResponse searchUsersWithSocial(@RequestParam("keyword") String keyword);
}
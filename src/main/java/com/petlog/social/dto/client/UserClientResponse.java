package com.petlog.social.dto.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserClientResponse {
    private Long userId;
    private String email;
    private String username;     // 닉네임
    private String profileImage; // 프로필 이미지 URL
    private String social;       // 소셜 아이디 (ex: @choco_mom)
}
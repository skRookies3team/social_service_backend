package com.petlog.social.dto.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserClientResponse {
    private Long userId;
    private String nickname;
    private String profileImageUrl; // 프로필 사진도 필요하면 사용
}
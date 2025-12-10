package com.petlog.social.dto.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserClientResponse {
    private String username;      // 기존 nickname -> username
    private String profileImage;  // 기존 profileImageUrl -> profileImage

    // (참고: User Service의 GetUserDto에는 userId가 포함되지 않아 null이 될 수 있음.
    // ID는 이미 알고 요청하므로 굳이 없어도 무방함)
    private Long userId;
}
package com.petlog.social.dto.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserClientResponse {
    // User Service의 GetSearchedUserDto 필드와 일치
    private Long userId;
    private String username;
    private String genderType; // Enum이면 String으로 매핑되거나 별도 처리 필요
    private String profileImage;
    private String social;
    private String statusMessage;
    private Integer age;
}
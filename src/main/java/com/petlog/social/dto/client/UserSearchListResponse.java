package com.petlog.social.dto.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class UserSearchListResponse {
    // User Service의 GetSearchedUserDtoList 내부 필드명과 일치해야 함 (보통 users 또는 userList)
    private List<UserClientResponse> users;
}
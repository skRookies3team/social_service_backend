package com.petlog.social.dto.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserSearchListResponse {

    // User Service의 GetSearchedUserDtoList 필드명과 일치해야 함
    private boolean isEmpty;
    private List<UserClientResponse> users;
}
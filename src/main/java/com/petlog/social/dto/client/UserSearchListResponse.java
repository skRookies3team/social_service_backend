package com.petlog.social.dto.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class UserSearchListResponse {
    private List<UserClientResponse> users;
}
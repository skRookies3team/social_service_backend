package com.petlog.social.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowListResponse {
    @Schema(description = "유저 ID")
    private Long userId;

    @Schema(description = "유저 닉네임")
    private String nickname;
}
package com.example.petlog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FollowStatResponse {
    @Schema(description = "나를 팔로우 한 사람 수 (팔로워)")
    private long followerCount;

    @Schema(description = "내가 한 팔로우 수 (팔로잉)")
    private long followingCount;
}
package com.example.petlog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class FeedLikeResponse {

    @Getter
    @Builder
    public static class LikerDto {
        @Schema(description = "좋아요 누른 유저 ID")
        private Long userId;

        @Schema(description = "유저 닉네임")
        private String nickname;

        // 프로필 이미지 URL 등이 필요하면 UserClient 수정 후 추가 가능
    }
}
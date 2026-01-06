package com.petlog.social.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class FeedLikeResponse {

    // 좋아요 누른 사람 목록 조회용 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikerDto {
        @Schema(description = "좋아요 누른 유저 ID")
        private Long userId;

        @Schema(description = "유저 닉네임")
        private String nickname;

        @Schema(description = "프로필 이미지 URL")
        private String profileImage;
    }

    // [핵심] 토글 결과 반환용 DTO (프론트엔드 상태 동기화용)
    @Data
    @AllArgsConstructor
    public static class ToggleLikeResponse {
        private boolean isLiked; // 변경된 내 상태 (true: 좋아요 됨, false: 취소됨)
        private long likeCount;  // 갱신된 총 좋아요 개수
    }
}
package com.petlog.social.dto.response;

import com.petlog.social.entity.Feed;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class FeedResponse {

    @Getter
    @Builder
    public static class GetFeedDto {
        @Schema(description = "피드 ID")
        private Long feedId;

        // --- 작성자 정보 (User Service 연동) ---
        @Schema(description = "작성자 유저 ID")
        private Long writerId;
        @Schema(description = "작성자 소셜 ID")
        private String writerSocialId;
        @Schema(description = "작성자 닉네임")
        private String writerNickname;
        @Schema(description = "작성자 프로필 이미지")
        private String writerProfileImage;
        // ------------------------------------

        @Schema(description = "반려동물 이름")
        private String petName;
        @Schema(description = "내용")
        private String content;
        @Schema(description = "이미지 URL 목록")
        private List<String> imageUrls;
        @Schema(description = "위치")
        private String location;

        @Schema(description = "좋아요 수")
        private long likeCount;
        @Schema(description = "좋아요 여부")
        private boolean isLiked;
        @Schema(description = "댓글 수")
        private Long commentCount;
        @Schema(description = "해시태그 목록")
        private List<String> hashtags;
        @Schema(description = "최신 댓글 미리보기")
        private List<CommentResponse.CommentDto> recentComments;
        @Schema(description = "작성일")
        private LocalDateTime createdAt;
    }
}
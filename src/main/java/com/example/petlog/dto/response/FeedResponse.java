package com.example.petlog.dto.response;

import com.example.petlog.entity.Feed;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

// 피드 관련 응답 데이터를 담는 클래스
public class FeedResponse {

    // 단일 피드 조회 응답 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetFeedDto {
        @Schema(description = "피드 ID")
        private Long feedId;

        @Schema(description = "작성자 닉네임")
        private String writerNickname;

        @Schema(description = "반려동물 이름")
        private String petName;

        @Schema(description = "피드 내용")
        private String content;

        @Schema(description = "이미지 URL")
        private String imageUrl;

        @Schema(description = "작성일시")
        private LocalDateTime createdAt;

        // 엔티티를 DTO로 변환하는 정적 메서드
        public static GetFeedDto fromEntity(Feed feed) {
            return GetFeedDto.builder()
                    .feedId(feed.getId())
                    .writerNickname(feed.getUser().getNickname())
                    .petName(feed.getPet() != null ? feed.getPet().getName() : null)
                    .content(feed.getContent())
                    .imageUrl(feed.getImageUrl())
                    .createdAt(feed.getCreatedAt())
                    .build();
        }
    }
}
package com.example.petlog.dto.response;

import com.example.petlog.entity.Feed;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

public class FeedResponse {

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

        @Schema(description = "이미지 URL (Base64 문자열)")
        private String imageUrl;

        @Schema(description = "작성일시")
        private LocalDateTime createdAt;

        @Schema(description = "위치정보")
        private String location;

        public static GetFeedDto of(Feed feed, String writerNickname, String petName, String imageBase64) {
            return GetFeedDto.builder()
                    .feedId(feed.getId())
                    .writerNickname(writerNickname)
                    .petName(petName)
                    .content(feed.getContent())
                    .location(feed.getLocation())
                    .imageUrl(imageBase64) //
                    .createdAt(feed.getCreatedAt())
                    .build();
        }
    }
}
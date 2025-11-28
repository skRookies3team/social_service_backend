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

        @Schema(description = "이미지 URL")
        private String imageUrl;

        @Schema(description = "작성일시")
        private LocalDateTime createdAt;

        // MSA 환경에서는 닉네임과 펫 이름을 외부에서 받아와야 하므로 파라미터 추가
        public static GetFeedDto of(Feed feed, String writerNickname, String petName) {
            return GetFeedDto.builder()
                    .feedId(feed.getId())
                    .writerNickname(writerNickname)
                    .petName(petName)
                    .content(feed.getContent())
                    .imageUrl(feed.getImageUrl())
                    .createdAt(feed.getCreatedAt())
                    .build();
        }
    }
}
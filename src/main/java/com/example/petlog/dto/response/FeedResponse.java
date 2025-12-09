package com.example.petlog.dto.response;

import com.example.petlog.entity.Feed;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

        @Schema(description = "좋아요수")
        private long likeCount;

        @Schema(description = "내가 좋아요를 눌렀는지 여부 (true: 누름, false: 안누름)")
        private boolean isLiked;

        @Schema(description = "작성일시")
        private LocalDateTime createdAt;

        @Schema(description = "위치정보")
        private String location;

        @Schema(description = "전체 댓글 수")
        private Long commentCount;

        @Schema(description = "최신 댓글 미리보기 최대 3개까지 보여줌 ")
        private List<CommentResponse.CommentDto> recentComments;

        @Schema(description = "해시태그 목록")
        private List<String> hashtags;

        public static GetFeedDto of(Feed feed, String writerNickname, String petName, String imageUrl, long likeCount, boolean isLiked, Long commentCount, List<CommentResponse.CommentDto> recentComments, List<String> hashtags) {
            return GetFeedDto.builder()
                    .feedId(feed.getId())
                    .writerNickname(writerNickname)
                    .petName(petName)
                    .content(feed.getContent())
                    .location(feed.getLocation())
                    .imageUrl(imageUrl)
                    .likeCount(likeCount)
                    .isLiked(isLiked)
                    .commentCount(commentCount)
                    .recentComments(recentComments)
                    .hashtags(hashtags)
                    .createdAt(feed.getCreatedAt())
                    .build();
        }
    }
}
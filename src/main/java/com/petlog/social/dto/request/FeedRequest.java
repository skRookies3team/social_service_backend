package com.petlog.social.dto.request;

import com.petlog.social.entity.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class FeedRequest {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateFeedDto {
        @Schema(description = "작성자 ID (User Service PK)", example = "1")
        @NotNull(message = "작성자 ID는 필수입니다.")
        private Long userId;

        @Schema(description = "반려동물 ID (선택)", example = "1")
        private Long petId;

        @Schema(description = "게시물 내용", example = "오늘 날씨가 너무 좋네요!")
        private String content;

        @Schema(description = "위치 정보", example = "서울숲")
        private String location;

        @Schema(description = "이미지 URL 목록", example = "[\"http://url1.jpg\", \"http://url2.jpg\"]")
        private List<String> imageUrls;

        @Schema(description = "공개 범위 (PUBLIC, FOLLOWER, PRIVATE)", example = "PUBLIC")
        private Visibility visibility; // Enum 추가 필요
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdateFeedDto {
        @Schema(description = "작성자 ID (본인 확인용)", example = "1")
        @NotNull
        private Long userId;

        @Schema(description = "수정할 내용")
        private String content;

        @Schema(description = "수정할 이미지 URL 목록")
        private List<String> imageUrls;

        @Schema(description = "수정할 위치")
        private String location;
    }
}
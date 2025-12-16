package com.petlog.social.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

        @Schema(description = "이미지 URL (User Service에서 업로드 후 받은 URL)", example = "https://s3.../image.jpg")
        private String imageUrl; // [변경] 파일 대신 URL 문자열을 받음
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

        @Schema(description = "수정할 이미지 URL")
        private String imageUrl;

        @Schema(description = "수정할 위치")
        private String location;
    }
}
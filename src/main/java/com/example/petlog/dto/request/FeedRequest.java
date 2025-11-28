package com.example.petlog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 피드 관련 요청 데이터를 담는 클래스
public class FeedRequest {

    // 피드 생성 요청 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateFeedDto {
        @NotNull(message = "작성자 ID는 필수입니다.")
        @Schema(description = "작성자 ID", example = "1")
        private Long userId;

        @Schema(description = "관련된 반려동물 ID (선택 입력)", example = "1")
        private Long petId;

        @NotBlank(message = "피드 내용은 필수입니다.")
        @Schema(description = "피드 내용", example = "오늘 산책 너무 즐거웠어!")
        private String content;

        @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
        private String imageUrl;
    }

    // 피드 수정 요청 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdateFeedDto {

        @NotNull(message = "사용자 ID는 필수입니다.") // 추가
        @Schema(description = "작성자 ID (본인 확인용)", example = "1")
        private Long userId;

        @NotBlank(message = "피드 내용은 필수입니다.")
        @Schema(description = "수정할 피드 내용", example = "내용 수정됨")
        private String content;

        @Schema(description = "수정할 이미지 URL", example = "https://example.com/new_image.jpg")
        private String imageUrl;

        @Schema(description = "위치 정보", example = "부산 해운대")
        private String location;
    }
}
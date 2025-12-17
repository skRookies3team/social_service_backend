package com.petlog.social.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class CommentRequest {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateDto {
        @NotNull(message = "작성자 ID는 필수입니다.")
        private Long userId;

        @Schema(description = "피드 ID (URL 경로로 자동 주입)", hidden = true)
        // [중요] @NotNull 제거 (컨트롤러 진입 전 검증 실패 방지)
        private Long feedId;

        @NotBlank(message = "댓글 내용은 필수입니다.")
        private String content;

        @Schema(description = "부모 댓글 ID (대댓글인 경우에만 입력)", nullable = true)
        private Long parentId;
    }
}
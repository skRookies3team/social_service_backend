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

        // [수정] @NotNull 제거 (컨트롤러에서 PathVariable로 주입하므로 검증 제외)
        @Schema(description = "피드 ID (URL 파라미터로 자동 주입됨)", example = "1", hidden = true)
        private Long feedId;

        @NotBlank(message = "댓글 내용은 필수입니다.")
        private String content;

        @Schema(description = "부모 댓글 ID (대댓글일 경우에만 입력, 원댓글이면 null)", nullable = true)
        private Long parentId;
    }
}
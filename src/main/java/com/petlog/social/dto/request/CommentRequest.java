package com.petlog.social.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class CommentRequest {

    @Getter
    @Setter
    public static class CreateDto {
        @NotNull
        private Long userId;

        @NotBlank
        private String content;

        private Long feedId;
        private Long parentId; // 대댓글일 경우 부모 댓글 ID
    }

    // [추가] 댓글 수정 요청 DTO
    @Getter
    @Setter
    public static class UpdateDto {
        @NotNull
        private Long userId;

        @NotBlank
        private String content;
    }
}
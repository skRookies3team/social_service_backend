package com.example.petlog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentRequest {
    @NotNull(message = "작성자 ID는 필수입니다.")
    private Long userId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    @Schema(description = "부모 댓글 ID (대댓글일 경우에만 입력, 원댓글이면 null)", nullable = true)
    private Long parentId;
}
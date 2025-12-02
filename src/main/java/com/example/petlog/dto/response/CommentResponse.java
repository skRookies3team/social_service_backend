package com.example.petlog.dto.response;

import com.example.petlog.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CommentResponse {

    @Getter
    @Builder
    public static class CommentDto {
        private Long commentId;
        private Long userId;
        private String nickname;
        private String content;
        private LocalDateTime createdAt;

        // 대댓글 리스트 (자식들)
        private List<CommentDto> children;

        // 엔티티 -> DTO 변환 메서드 (재귀적으로 자식들도 변환)
        public static CommentDto of(Comment comment, String nickname) {
            return CommentDto.builder()
                    .commentId(comment.getId())
                    .userId(comment.getUserId())
                    .nickname(nickname)
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    // 자식 댓글이 있으면 DTO로 변환해서 넣고, 없으면 빈 리스트
                    .children(comment.getChildren().stream()
                            .map(child -> CommentDto.of(child, nickname)) // 닉네임 로직은 서비스에서 처리 필요하지만 간소화를 위해 일단 전달
                            .collect(Collectors.toList()))
                    .build();
        }
    }
}
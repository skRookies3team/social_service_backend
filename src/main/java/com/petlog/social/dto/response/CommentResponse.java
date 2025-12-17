package com.petlog.social.dto.response;

import com.petlog.social.dto.client.UserClientResponse;
import com.petlog.social.entity.Comment;
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
        private Long feedId;

        // --- 댓글 작성자 정보 ---
        private Long writerId;
        private String writerSocialId;
        private String writerNickname;
        private String writerProfileImage;
        private List<CommentDto> children; // 대댓글 리스트
        // ---------------------

        private String content;
        private LocalDateTime createdAt;

        // 엔티티 + 유저정보 -> DTO 변환 메서드
        public static CommentDto of(Comment comment, UserClientResponse user) {
            return CommentDto.builder()
                    .commentId(comment.getId())
                    .feedId(comment.getFeed().getId())
                    .writerId(comment.getUserId())
                    .writerSocialId(user != null ? user.getSocial() : "")
                    .writerNickname(user != null ? user.getUsername() : "알 수 없음")
                    .writerProfileImage(user != null ? user.getProfileImage() : null)
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .children(comment.getChildren().stream()
                            .map(child -> CommentDto.of(child, null)) // 자식의 유저 정보는 일단 null 또는 별도 로직 처리
                            .collect(Collectors.toList()))
                    .build();
        }
    }
}
package com.petlog.social.dto.response;

import com.petlog.social.dto.client.UserClientResponse;
import com.petlog.social.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class CommentResponse {

    @Getter
    @Builder
    public static class CommentDto {
        private Long commentId;
        private Long feedId;

        // --- 작성자 정보 (User Service 연동) ---
        private Long writerId;
        private String writerNickname;
        private String writerProfileImage;
        private String writerSocialId;
        // ------------------------------------

        private String content;
        private LocalDateTime createdAt;
        private Long parentId; // 대댓글일 경우 부모 ID

        public static CommentDto of(Comment comment, UserClientResponse user) {
            return CommentDto.builder()
                    .commentId(comment.getId())
                    .feedId(comment.getFeed().getId())
                    .writerId(comment.getUserId())
                    // 유저 정보가 없으면(null) 기본값 처리
                    .writerNickname(user != null ? user.getUsername() : "알 수 없음")
                    .writerProfileImage(user != null ? user.getProfileImage() : null)
                    .writerSocialId(user != null ? user.getSocial() : "")
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                    .build();
        }
    }
}
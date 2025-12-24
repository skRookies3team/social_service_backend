package com.petlog.social.dto.response;

import com.petlog.social.dto.client.UserClientResponse;
import com.petlog.social.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommentResponse {

    @Getter
    @Builder
    public static class CommentDto {
        private Long commentId;
        private Long feedId;
        private Long writerId;
        private String writerNickname;
        private String writerProfileImage;
        private String writerSocialId;
        private String content;
        private LocalDateTime createdAt;
        private Long parentId;

        // [추가] 대댓글 리스트 (계층 구조)
        private List<CommentDto> children;

        public static CommentDto of(Comment comment, UserClientResponse user) {
            return CommentDto.builder()
                    .commentId(comment.getId())
                    .feedId(comment.getFeed().getId())
                    .writerId(comment.getUserId())
                    .writerNickname(user != null ? user.getUsername() : "알 수 없음")
                    .writerProfileImage(user != null ? user.getProfileImage() : null)
                    .writerSocialId(user != null ? user.getSocial() : "")
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                    // [추가] 자식 댓글 재귀 매핑 (User 정보는 성능상 여기선 생략하거나 배치 로딩 필요. 일단 단순화)
                    .children(comment.getChildren() != null
                            ? comment.getChildren().stream()
                            .map(c -> CommentDto.of(c, null)) // 자식의 유저 정보는 로직에 따라 별도 처리 필요
                            .collect(Collectors.toList())
                            : Collections.emptyList())
                    .build();
        }

        // 자식 댓글에 유저 정보를 채워넣기 위한 Setter/Method
        public void setChildren(List<CommentDto> children) {
            this.children = children;
        }
    }
}
package com.petlog.social.service.impl;

import com.petlog.social.client.UserClient;
import com.petlog.social.dto.client.UserClientResponse;
import com.petlog.social.dto.request.CommentRequest;
import com.petlog.social.dto.response.CommentResponse;
import com.petlog.social.entity.Comment;
import com.petlog.social.entity.Feed;
import com.petlog.social.exception.BusinessException;
import com.petlog.social.exception.EntityNotFoundException;
import com.petlog.social.exception.ErrorCode;
import com.petlog.social.repository.CommentRepository;
import com.petlog.social.repository.FeedRepository;
import com.petlog.social.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final FeedRepository feedRepository;
    private final UserClient userClient;

    @Override
    @Transactional
    public Long createComment(CommentRequest.CreateDto request) {
        // 1. 피드 조회
        Feed feed = feedRepository.findById(request.getFeedId())
                .orElseThrow(() -> new EntityNotFoundException("Feed", request.getFeedId()));

        // 2. 부모 댓글 조회 (대댓글인 경우)
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Comment", request.getParentId()));
        }

        // 3. 저장
        Comment comment = Comment.builder()
                .userId(request.getUserId())
                .content(request.getContent())
                .feed(feed)
                .parent(parent)
                .build();

        return commentRepository.save(comment).getId();
    }

    @Override
    public List<CommentResponse.CommentDto> getComments(Long feedId) {
        // 1. 부모 댓글(최상위 댓글)만 DB에서 조회
        // JPA의 @OneToMany 관계 설정을 통해 children은 Lazy Loading으로 가져옵니다.
        List<Comment> comments = commentRepository.findAllByFeedIdAndParentIsNullOrderByCreatedAtAsc(feedId);

        return comments.stream()
                .map(comment -> {
                    // 2. 부모 댓글 유저 정보 조회 및 DTO 변환
                    UserClientResponse user = fetchUserSafe(comment.getUserId());
                    CommentResponse.CommentDto dto = CommentResponse.CommentDto.of(comment, user);

                    // 3. 대댓글(자식) 유저 정보 조회 및 DTO 변환
                    if (comment.getChildren() != null) {
                        List<CommentResponse.CommentDto> childDtos = comment.getChildren().stream()
                                .map(child -> {
                                    UserClientResponse childUser = fetchUserSafe(child.getUserId());
                                    return CommentResponse.CommentDto.of(child, childUser);
                                })
                                .collect(Collectors.toList());

                        dto.setChildren(childDtos);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateComment(Long commentId, CommentRequest.UpdateDto request, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment", commentId));

        // 작성자 본인 확인
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FEED_UNAUTHORIZED);
        }

        comment.updateContent(request.getContent());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment", commentId));

        // 작성자 본인 확인
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FEED_UNAUTHORIZED);
        }
        commentRepository.delete(comment);
    }

    /**
     * 유저 정보 조회 안전 처리 헬퍼 메서드
     * User Service 호출 실패 시에도 댓글 목록 조회는 성공하도록 null 반환 처리
     */
    private UserClientResponse fetchUserSafe(Long userId) {
        try {
            return userClient.getUser(userId);
        } catch (Exception e) {
            log.warn("Comment User fetch failed (userId={}): {}", userId, e.getMessage());
            return null;
        }
    }
}
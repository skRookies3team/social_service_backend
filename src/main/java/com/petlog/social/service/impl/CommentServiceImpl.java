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
    private final UserClient userClient; // [추가] 유저 정보 조회용

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
        // 해당 피드의 모든 댓글 조회
        // (만약 대댓글 구조를 위해 부모만 가져오고 싶다면 리포지토리 메서드 수정 필요)
        List<Comment> comments = commentRepository.findAllByFeedIdAndParentIsNullOrderByCreatedAtDesc(feedId);

        return comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment", commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FEED_UNAUTHORIZED);
        }
        commentRepository.delete(comment);
    }

    // [리팩토링] DTO 변환 시 User Service 호출
    private CommentResponse.CommentDto convertToDto(Comment comment) {
        UserClientResponse user = null;
        try {
            user = userClient.getUser(comment.getUserId());
        } catch (Exception e) {
            log.warn("Comment User fetch failed (userId={}): {}", comment.getUserId(), e.getMessage());
        }
        return CommentResponse.CommentDto.of(comment, user);
    }
}
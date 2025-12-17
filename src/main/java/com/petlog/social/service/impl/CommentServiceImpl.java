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
    public Long createComment(CommentRequest.CreateDto request) { // [수정] 파라미터 변경
        Feed feed = feedRepository.findById(request.getFeedId())
                .orElseThrow(() -> new EntityNotFoundException("Feed", request.getFeedId()));

        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Comment", request.getParentId()));
        }

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
            // [수정] ErrorCode 상수명 맞춤
            throw new BusinessException(ErrorCode.FEED_UNAUTHORIZED);
        }
        commentRepository.delete(comment);
    }

    private CommentResponse.CommentDto convertToDto(Comment comment) {
        UserClientResponse user = null;
        try {
            user = userClient.getUser(comment.getUserId());
        } catch (Exception e) {
            log.warn("User Service Error: {}", e.getMessage());
        }
        return CommentResponse.CommentDto.of(comment, user);
    }
}
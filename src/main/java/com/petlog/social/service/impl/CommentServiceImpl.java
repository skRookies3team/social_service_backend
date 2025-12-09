package com.petlog.social.service.impl;

import com.petlog.social.client.UserClient;
import com.petlog.social.dto.client.UserClientResponse; // ✅ DTO Import 추가
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
    public void createComment(Long feedId, CommentRequest request) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));

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

        commentRepository.save(comment);
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
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "본인의 댓글만 삭제할 수 있습니다.");
        }
        commentRepository.delete(comment);
    }

    // DTO 변환 헬퍼 메서드
    private CommentResponse.CommentDto convertToDto(Comment comment) {
        String nickname = "Unknown";
        try {
            // 🚨 [수정됨] getNickname() -> getUser().getNickname()
            UserClientResponse userDto = userClient.getUser(comment.getUserId());
            if (userDto != null) {
                nickname = userDto.getNickname();
            }
        } catch (Exception e) {
            log.warn("User Service Error: {}", e.getMessage());
        }
        return CommentResponse.CommentDto.of(comment, nickname);
    }
}
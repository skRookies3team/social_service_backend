package com.petlog.social.service.impl;

import com.petlog.social.client.UserClient;
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
        // 대댓글인 경우 부모 댓글 조회
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Comment", request.getParentId()));
        }

        Comment comment = Comment.builder()
                .userId(request.getUserId())
                .content(request.getContent())
                .feed(feed)
                .parent(parent) // 부모 설정 (없으면 null)
                .build();

        commentRepository.save(comment);
    }

    @Override
    public List<CommentResponse.CommentDto> getComments(Long feedId) {
        // 최상위 댓글만 가져옴 (자식은 엔티티 안에 들어있음)
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

    // DTO 변환 헬퍼 메서드 (닉네임 조회 포함)
    private CommentResponse.CommentDto convertToDto(Comment comment) {
        String nickname = "Unknown";
        try {
            nickname = userClient.getNickname(comment.getUserId());
        } catch (Exception e) {
            log.warn("User Service Error: {}", e.getMessage());
        }
        // 자식 댓글들의 닉네임 처리는 복잡도를 줄이기 위해 여기서 재귀적으로 하진 않았지만,
        // 실제로는 자식 댓글의 userId로도 닉네임을 조회해야 합니다.
        // 지금은 부모 닉네임만 조회하고 자식은 of 메서드에서 처리하도록 둡니다.
        return CommentResponse.CommentDto.of(comment, nickname);
    }
}
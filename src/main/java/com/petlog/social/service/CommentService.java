package com.petlog.social.service;

import com.petlog.social.dto.request.CommentRequest;
import com.petlog.social.dto.response.CommentResponse;
import java.util.List;

public interface CommentService {
    // [수정] DTO 하나만 받도록 변경
    Long createComment(CommentRequest.CreateDto request);

    List<CommentResponse.CommentDto> getComments(Long feedId);

    void deleteComment(Long commentId, Long userId);
}
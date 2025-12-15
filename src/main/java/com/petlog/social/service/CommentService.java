package com.petlog.social.service;

import com.petlog.social.dto.request.CommentRequest;
import com.petlog.social.dto.response.CommentResponse;
import java.util.List;

public interface CommentService {
    void createComment(Long feedId, CommentRequest request);
    List<CommentResponse.CommentDto> getComments(Long feedId); // 전체보기
    void deleteComment(Long commentId, Long userId);
}
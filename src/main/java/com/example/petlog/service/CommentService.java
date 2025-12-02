package com.example.petlog.service;

import com.example.petlog.dto.request.CommentRequest;
import com.example.petlog.dto.response.CommentResponse;
import java.util.List;

public interface CommentService {
    void createComment(Long feedId, CommentRequest request);
    List<CommentResponse.CommentDto> getComments(Long feedId); // 전체보기
    void deleteComment(Long commentId, Long userId);
}
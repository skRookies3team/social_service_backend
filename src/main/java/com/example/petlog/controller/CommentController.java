package com.example.petlog.controller;

import com.example.petlog.dto.request.CommentRequest;
import com.example.petlog.dto.response.CommentResponse;
import com.example.petlog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Social Comment", description = "댓글 API")
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성 (대댓글 포함)
    @PostMapping("/feeds/{feedId}/comments")
    @Operation(summary = "댓글 작성", description = "parentId가 있으면 대댓글, 없으면 원댓글이 됩니다.")
    public ResponseEntity<String> createComment(
            @PathVariable Long feedId,
            @Valid @RequestBody CommentRequest request) {
        commentService.createComment(feedId, request);
        return ResponseEntity.ok("댓글이 작성되었습니다.");
    }

    // 전체 댓글 조회 (상세보기용)
    @GetMapping("/feeds/{feedId}/comments")
    @Operation(summary = "댓글 전체 조회", description = "해당 피드의 모든 댓글을 계층 구조로 조회합니다.")
    public ResponseEntity<List<CommentResponse.CommentDto>> getComments(@PathVariable Long feedId) {
        return ResponseEntity.ok(commentService.getComments(feedId));
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }
}
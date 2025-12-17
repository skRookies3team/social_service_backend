package com.petlog.social.controller;

import com.petlog.social.dto.request.CommentRequest;
import com.petlog.social.dto.response.CommentResponse;
import com.petlog.social.service.CommentService;
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

    // 댓글 작성
    @PostMapping("/feeds/{feedId}/comments")
    @Operation(summary = "댓글 작성", description = "parentId가 있으면 대댓글, 없으면 원댓글이 됩니다.")
    public ResponseEntity<String> createComment(
            @PathVariable Long feedId,
            @Valid @RequestBody CommentRequest.CreateDto request // [수정] CreateDto 사용
    ) {
        // URL에서 받은 feedId 주입
        request.setFeedId(feedId);

        commentService.createComment(request);
        return ResponseEntity.ok("댓글이 작성되었습니다.");
    }

    // 댓글 목록 조회
    @GetMapping("/feeds/{feedId}/comments")
    @Operation(summary = "댓글 전체 조회", description = "해당 피드의 댓글 목록을 조회합니다.")
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
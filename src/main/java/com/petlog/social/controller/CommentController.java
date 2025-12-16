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
@RequestMapping("/api") // 클래스 레벨 매핑
@RequiredArgsConstructor
@Tag(name = "Social Comment", description = "댓글 API")
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    // URL: POST /api/feeds/{feedId}/comments
    @PostMapping("/feeds/{feedId}/comments")
    @Operation(summary = "댓글 작성", description = "parentId가 있으면 대댓글, 없으면 원댓글이 됩니다.")
    public ResponseEntity<String> createComment(
            @PathVariable Long feedId,
            @Valid @RequestBody CommentRequest.CreateDto request // [수정] DTO 타입 변경 (CreateDto)
    ) {
        // PathVariable로 받은 feedId를 DTO에 설정 (DTO에 setFeedId 메서드가 있어야 함)
        request.setFeedId(feedId);

        // [수정] 서비스 메서드 호출 (파라미터 1개)
        commentService.createComment(request);

        return ResponseEntity.ok("댓글이 작성되었습니다.");
    }

    // 전체 댓글 조회
    // URL: GET /api/feeds/{feedId}/comments
    @GetMapping("/feeds/{feedId}/comments")
    @Operation(summary = "댓글 전체 조회", description = "해당 피드의 모든 댓글을 계층 구조로 조회합니다.")
    public ResponseEntity<List<CommentResponse.CommentDto>> getComments(@PathVariable Long feedId) {
        return ResponseEntity.ok(commentService.getComments(feedId));
    }

    // 댓글 삭제
    // URL: DELETE /api/comments/{commentId}
    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }
}
package com.example.petlog.controller;

import com.example.petlog.service.FeedLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
@Tag(name = "Social Feed Like", description = "피드 좋아요 API") // Swagger 그룹 명시
public class FeedLikeController {

    private final FeedLikeService feedLikeService;

    @PostMapping("/{feedId}/likes")
    @Operation(summary = "좋아요 토글", description = "피드에 좋아요를 누르거나 취소합니다.") // API 설명
    @ApiResponse(responseCode = "200", description = "성공 메시지 반환 (좋아요 등록/취소)")
    public ResponseEntity<String> toggleLike(
            @Parameter(description = "대상 피드 ID", required = true)
            @PathVariable Long feedId,

            @Parameter(description = "좋아요 누르는 유저 ID", required = true)
            @RequestParam Long userId
    ) {
        boolean isLiked = feedLikeService.toggleLike(feedId, userId);

        if (isLiked) {
            return ResponseEntity.ok("좋아요를 눌렀습니다.");
        } else {
            return ResponseEntity.ok("좋아요를 취소했습니다.");
        }
    }
}
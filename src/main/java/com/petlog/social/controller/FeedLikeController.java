package com.petlog.social.controller;

import com.petlog.social.dto.response.FeedLikeResponse;
import com.petlog.social.service.FeedLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
@Tag(name = "Social Feed Like", description = "피드 좋아요 API")
public class FeedLikeController {

    private final FeedLikeService feedLikeService;

    // [수정] 좋아요 토글 API
    @PostMapping("/{feedId}/likes")
    @Operation(summary = "좋아요 토글", description = "피드에 좋아요를 누르거나 취소하고, 최신 상태와 개수를 반환합니다.")
    public ResponseEntity<FeedLikeResponse.ToggleLikeResponse> toggleLike(
            @PathVariable Long feedId,
            @RequestParam Long userId
    ) {
        // [수정] 단순 문자열 대신 상태 객체 반환
        return ResponseEntity.ok(feedLikeService.toggleLike(feedId, userId));
    }

    // 좋아요 누른 사람 목록 조회 API
    @GetMapping("/{feedId}/likes")
    @Operation(summary = "좋아요 누른 사람 목록 조회", description = "게시물 작성자(주인)만 조회할 수 있습니다.")
    public ResponseEntity<List<FeedLikeResponse.LikerDto>> getLikers(
            @Parameter(description = "피드 ID", required = true)
            @PathVariable Long feedId
    ) {
        return ResponseEntity.ok(feedLikeService.getLikers(feedId));
    }
}
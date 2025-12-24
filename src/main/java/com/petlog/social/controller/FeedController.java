package com.petlog.social.controller;

import com.petlog.social.dto.request.FeedRequest;
import com.petlog.social.dto.response.FeedResponse;
import com.petlog.social.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
@Tag(name = "Social Feed", description = "소셜 피드 API")
public class FeedController {

    private final FeedService feedService;

    // [Refactor] 이미지 파일 제거 -> URL 포함된 JSON 받기
    @PostMapping
    @Operation(summary = "피드 작성", description = "User Service에서 이미지를 업로드한 후 반환받은 URL을 imageUrl 필드에 담아 요청하세요.")
    public ResponseEntity<Long> createFeed(
            @Valid @RequestBody FeedRequest.CreateFeedDto request
    ) {
        System.out.println("🔥 [디버깅] 받은 이미지 리스트: " + request.getImageUrls());

        Long feedId = feedService.createFeed(request);
        return ResponseEntity.created(URI.create("/api/feeds/" + feedId)).body(feedId);
    }

    @GetMapping("/viewer/{userId}")
    @Operation(summary = "전체 피드 조회")
    public ResponseEntity<Slice<FeedResponse.GetFeedDto>> getAllFeeds(
            @PathVariable Long userId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(feedService.getAllFeeds(userId, pageable));
    }

    @GetMapping("/{feedId}/viewer/{userId}")
    @Operation(summary = "피드 상세 조회")
    public ResponseEntity<FeedResponse.GetFeedDto> getFeed(
            @PathVariable Long feedId,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(feedService.getFeed(feedId, userId));
    }

    @PutMapping("/{feedId}")
    @Operation(summary = "피드 수정")
    public ResponseEntity<Void> updateFeed(
            @PathVariable Long feedId,
            @Valid @RequestBody FeedRequest.UpdateFeedDto request) {
        feedService.updateFeed(feedId, request, request.getUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{feedId}")
    @Operation(summary = "피드 삭제")
    public ResponseEntity<Void> deleteFeed(@PathVariable Long feedId, @RequestParam Long userId) {
        feedService.deleteFeed(feedId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{targetUserId}/viewer/{viewerId}")
    @Operation(summary = "유저별 피드 조회 (마이페이지)")
    public ResponseEntity<Slice<FeedResponse.GetFeedDto>> getUserFeeds(
            @PathVariable Long targetUserId,
            @PathVariable Long viewerId,
            @PageableDefault(size = 12) Pageable pageable
    ) {
        return ResponseEntity.ok(feedService.getUserFeeds(targetUserId, viewerId, pageable));
    }

    @GetMapping("/following/viewer/{userId}")
    @Operation(summary = "팔로우 뉴스피드 조회")
    public ResponseEntity<Slice<FeedResponse.GetFeedDto>> getFollowingFeeds(
            @PathVariable("userId") Long viewerId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(feedService.getFollowingFeeds(viewerId, pageable));
    }
}
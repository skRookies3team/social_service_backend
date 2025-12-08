package com.example.petlog.controller;

import com.example.petlog.dto.request.FeedRequest;
import com.example.petlog.dto.response.FeedResponse;
import com.example.petlog.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
@Tag(name = "Social Feed", description = "소셜 피드 API")
public class FeedController {

    private final FeedService feedService;

    // 피드 작성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "피드 작성")
    public ResponseEntity<Long> createFeed(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "request") @Valid FeedRequest.CreateFeedDto request
    ) {
        Long feedId = feedService.createFeed(request, file);
        return ResponseEntity.created(URI.create("/api/feeds/" + feedId)).body(feedId);
    }

    // 전체 피드 조회 (Path Variable 사용)
    // URL 예시: GET /api/feeds/viewer/1
    @GetMapping("/viewer/{userId}")
    @Operation(summary = "전체 피드 조회", description = "로그인한 유저(viewer) 기준으로 좋아요 여부를 포함하여 조회합니다.")
    public ResponseEntity<List<FeedResponse.GetFeedDto>> getAllFeeds(
            @Parameter(description = "로그인한 유저 ID", required = true)
            @PathVariable Long userId // @RequestParam -> @PathVariable 변경
    ) {
        return ResponseEntity.ok(feedService.getAllFeeds(userId));
    }

    // 피드 상세 조회
    // URL 예시: GET /api/feeds/1/viewer/1
    @GetMapping("/{feedId}/viewer/{userId}")
    @Operation(summary = "피드 상세 조회", description = "특정 피드를 조회합니다.")
    public ResponseEntity<FeedResponse.GetFeedDto> getFeed(
            @Parameter(description = "피드 ID", required = true)
            @PathVariable Long feedId,

            @Parameter(description = "로그인한 유저 ID", required = true)
            @PathVariable Long userId // @RequestParam -> @PathVariable 변경
    ) {
        return ResponseEntity.ok(feedService.getFeed(feedId, userId));
    }

    // 피드 수정
    @PutMapping("/{feedId}")
    @Operation(summary = "피드 수정")
    public ResponseEntity<Void> updateFeed(
            @PathVariable Long feedId,
            @Valid @RequestBody FeedRequest.UpdateFeedDto request) {
        feedService.updateFeed(feedId, request, request.getUserId());
        return ResponseEntity.ok().build();
    }

    // 피드 삭제
    @DeleteMapping("/{feedId}")
    @Operation(summary = "피드 삭제")
    public ResponseEntity<Void> deleteFeed(@PathVariable Long feedId, @RequestParam Long userId) {
        feedService.deleteFeed(feedId, userId);
        return ResponseEntity.noContent().build();
    }

    // 특정 유저의 피드 모아보기 (마이페이지/상대방 프로필)
    @GetMapping("/user/{targetUserId}/viewer/{viewerId}")
    @Operation(summary = "유저별 피드 조회 (마이페이지)", description = "특정 유저(targetUserId)가 작성한 피드 목록을 조회합니다.")
    public ResponseEntity<List<FeedResponse.GetFeedDto>> getUserFeeds(
            @Parameter(description = "프로필 주인 ID", required = true)
            @PathVariable Long targetUserId,

            @Parameter(description = "보고 있는 사람 ID (좋아요 여부 확인용)", required = true)
            @PathVariable Long viewerId
    ) {
        return ResponseEntity.ok(feedService.getUserFeeds(targetUserId, viewerId));
    }

    @GetMapping("/following/viewer/{userId}")
    @Operation(summary = "팔로우 피드 조회", description = "내가 팔로우한 사용자들의 피드만 모아서 최신순으로 조회합니다.")
    public ResponseEntity<List<FeedResponse.GetFeedDto>> getFollowingFeeds(
            @Parameter(description = "로그인한 유저 ID", required = true)
            @PathVariable("userId") Long viewerId
    ) {
        return ResponseEntity.ok(feedService.getFollowingFeeds(viewerId));
    }
}
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

    // 피드 작성 API
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "피드 작성")
    public ResponseEntity<Long> createFeed(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "request") @Valid FeedRequest.CreateFeedDto request
    ) {
        Long feedId = feedService.createFeed(request, file);
        // 생성된 리소스의 위치(URI)와 ID를 함께 반환
        return ResponseEntity.created(URI.create("/api/feeds/" + feedId)).body(feedId);
    }

    // 전체 피드 조회 API
    @GetMapping
    @Operation(summary = "전체 피드 조회", description = "최신순으로 피드 목록을 조회합니다. 로그인한 경우 좋아요 상태가 반영됩니다.")
    public ResponseEntity<List<FeedResponse.GetFeedDto>> getAllFeeds(
            @Parameter(description = "로그인한 유저 ID (좋아요 여부 확인용, 비로그인 시 null)")
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(feedService.getAllFeeds(userId));
    }

    // 피드 상세 조회 API
    @GetMapping("/{feedId}")
    @Operation(summary = "피드 상세 조회", description = "특정 피드의 상세 정보를 조회합니다.")
    public ResponseEntity<FeedResponse.GetFeedDto> getFeed(
            @Parameter(description = "피드 ID", required = true)
            @PathVariable Long feedId,

            @Parameter(description = "로그인한 유저 ID (좋아요 여부 확인용, 비로그인 시 null)")
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(feedService.getFeed(feedId, userId));
    }

    // 피드 수정 API
    @GetMapping("/{feedId}")
    @Operation(summary = "피드 상세 조회", description = "특정 피드의 상세 정보를 조회합니다.")
    public ResponseEntity<FeedResponse.GetFeedDto> getFeed(
            @Parameter(description = "피드 ID", required = true)
            @PathVariable Long feedId,

            @Parameter(description = "로그인한 유저 ID (좋아요 여부 확인용, 비로그인 시 null)")
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(feedService.getFeed(feedId, userId));
    }
    // 피드 삭제 API
    @DeleteMapping("/{feedId}")
    @Operation(summary = "피드 삭제")
    public ResponseEntity<Void> deleteFeed(@PathVariable Long feedId, @RequestParam Long userId) {
        feedService.deleteFeed(feedId, userId);
        return ResponseEntity.noContent().build();
    }
}

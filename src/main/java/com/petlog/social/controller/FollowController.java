package com.petlog.social.controller;

import com.petlog.social.dto.response.FollowListResponse;
import com.petlog.social.dto.response.FollowStatResponse;
import com.petlog.social.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
@Tag(name = "Social Follow", description = "팔로우 API")
public class FollowController {

    private final FollowService followService;

    // URL 변경에 따른 호출 예시: POST /api/follows/{targetId}/follow
    @PostMapping("/{targetId}")
    @Operation(summary = "팔로우 토글", description = "특정 유저를 팔로우하거나 취소합니다.")
    public ResponseEntity<String> toggleFollow(
            @Parameter(description = "팔로우 대상 ID") @PathVariable Long targetId,
            @Parameter(description = "나의 ID") @RequestParam Long followerId
    ) {
        boolean isFollowed = followService.toggleFollow(followerId, targetId);
        return ResponseEntity.ok(isFollowed ? "팔로우했습니다." : "언팔로우했습니다.");
    }

    // GET /api/follows/{userId}/stats
    @GetMapping("/{userId}/stats") // [수정] URL 깔끔하게 변경 (follow-stats -> stats)
    @Operation(summary = "팔로우 통계 조회", description = "유저의 팔로워/팔로잉 숫자를 조회합니다.")
    public ResponseEntity<FollowStatResponse> getFollowStats(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowStats(userId));
    }

    // GET /api/follows/{userId}/followers
    @GetMapping("/{userId}/followers")
    @Operation(summary = "팔로워 목록 조회", description = "나를 팔로우하는 사용자 목록을 조회합니다.")
    public ResponseEntity<List<FollowListResponse>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowers(userId));
    }

    // GET /api/follows/{userId}/followings
    @GetMapping("/{userId}/followings")
    @Operation(summary = "팔로잉 목록 조회", description = "내가 팔로우하는 사용자 목록을 조회합니다.")
    public ResponseEntity<List<FollowListResponse>> getFollowings(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowings(userId));
    }
}
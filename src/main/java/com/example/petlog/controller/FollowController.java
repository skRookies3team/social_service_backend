package com.example.petlog.controller;

import com.example.petlog.dto.response.FollowListResponse;
import com.example.petlog.dto.response.FollowStatResponse;
import com.example.petlog.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Social Follow", description = "팔로우 API")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{targetId}/follow")
    @Operation(summary = "팔로우 토글", description = "특정 유저를 팔로우하거나 취소합니다.")
    public ResponseEntity<String> toggleFollow(
            @Parameter(description = "팔로우 대상 ID") @PathVariable Long targetId,
            @Parameter(description = "나의 ID") @RequestParam Long followerId
    ) {
        boolean isFollowed = followService.toggleFollow(followerId, targetId);
        return ResponseEntity.ok(isFollowed ? "팔로우했습니다." : "언팔로우했습니다.");
    }

    @GetMapping("/{userId}/follow-stats")
    @Operation(summary = "팔로우 통계 조회", description = "유저의 팔로워/팔로잉 숫자를 조회합니다.")
    public ResponseEntity<FollowStatResponse> getFollowStats(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowStats(userId));
    }

    @GetMapping("/{userId}/followers")
    @Operation(summary = "팔로워 목록 조회", description = "나를 팔로우하는 사용자 목록을 조회합니다.")
    public ResponseEntity<List<FollowListResponse>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowers(userId));
    }

    @GetMapping("/{userId}/followings")
    @Operation(summary = "팔로잉 목록 조회", description = "내가 팔로우하는 사용자 목록을 조회합니다.")
    public ResponseEntity<List<FollowListResponse>> getFollowings(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowings(userId));
    }
}
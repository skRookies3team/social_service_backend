package com.example.petlog.service;

import com.example.petlog.dto.response.FollowListResponse;
import com.example.petlog.dto.response.FollowStatResponse;
import java.util.List;

public interface FollowService {
    // 팔로우 토글
    boolean toggleFollow(Long followerId, Long targetId);

    // 통계 조회
    FollowStatResponse getFollowStats(Long userId);

    // 목록 조회
    List<FollowListResponse> getFollowers(Long userId);
    List<FollowListResponse> getFollowings(Long userId);
}
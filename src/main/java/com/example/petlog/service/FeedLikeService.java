package com.example.petlog.service;

import com.example.petlog.dto.response.FeedLikeResponse;

import java.util.List;

public interface FeedLikeService {
    /**
     * 좋아요 토글 (누르면 등록, 다시 누르면 취소)
     * @param feedId 피드 ID
     * @param userId 유저 ID
     * @return true(등록됨), false(취소됨)
     */
    boolean toggleLike(Long feedId, Long userId);

    // 좋아요 누른 사람 목록 조회
    List<FeedLikeResponse.LikerDto> getLikers(Long feedId);
}
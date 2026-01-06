package com.petlog.social.service;

import com.petlog.social.dto.response.FeedLikeResponse;
import java.util.List;

public interface FeedLikeService {
    // 반환 타입 변경: boolean -> ToggleLikeResponse
    FeedLikeResponse.ToggleLikeResponse toggleLike(Long feedId, Long userId);

    List<FeedLikeResponse.LikerDto> getLikers(Long feedId);
}
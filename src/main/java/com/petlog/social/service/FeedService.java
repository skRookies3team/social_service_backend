package com.petlog.social.service;

import com.petlog.social.dto.request.FeedRequest;
import com.petlog.social.dto.response.FeedResponse;
import com.petlog.social.dto.response.SearchResponse; // DTO import 필요
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface FeedService {
    // [복구] MultipartFile 제거 -> String imageUrl로 처리
    Long createFeed(FeedRequest.CreateFeedDto request);

    Slice<FeedResponse.GetFeedDto> getAllFeeds(Long userId, Pageable pageable);

    FeedResponse.GetFeedDto getFeed(Long feedId, Long userId);

    void updateFeed(Long feedId, FeedRequest.UpdateFeedDto request, Long userId);

    void deleteFeed(Long feedId, Long userId);

    Slice<FeedResponse.GetFeedDto> getUserFeeds(Long targetUserId, Long viewerId, Pageable pageable);

    Slice<FeedResponse.GetFeedDto> getFollowingFeeds(Long viewerId, Pageable pageable);

    // [복구] 인기 게시물 조회
    Slice<FeedResponse.GetFeedDto> getTrendingFeeds(Long viewerId, Pageable pageable);

    // [복구] 통합 검색
    SearchResponse searchAll(String query, Long viewerId, Pageable pageable);
}
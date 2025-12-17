package com.petlog.social.service;

import com.petlog.social.dto.request.FeedRequest;
import com.petlog.social.dto.response.FeedResponse;
import com.petlog.social.dto.responst .SearchResponse; // [Import 확인]
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface FeedService {
    Long createFeed(FeedRequest.CreateFeedDto request);

    Slice<FeedResponse.GetFeedDto> getAllFeeds(Long userId, Pageable pageable);

    FeedResponse.GetFeedDto getFeed(Long feedId, Long userId);

    void updateFeed(Long feedId, FeedRequest.UpdateFeedDto request, Long userId);

    void deleteFeed(Long feedId, Long userId);

    Slice<FeedResponse.GetFeedDto> getUserFeeds(Long targetUserId, Long viewerId, Pageable pageable);

    Slice<FeedResponse.GetFeedDto> getFollowingFeeds(Long viewerId, Pageable pageable);

    // [복구] 인기 게시물 조회
    Slice<FeedResponse.GetFeedDto> getTrendingFeeds(Long viewerId, Pageable pageable);

    // [복구] 통합 검색 (SearchController에서 호출)
    SearchResponse searchAll(String query, Long viewerId, Pageable pageable);
}
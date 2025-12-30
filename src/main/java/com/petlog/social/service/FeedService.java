package com.petlog.social.service;

import com.petlog.social.dto.request.FeedRequest;
import com.petlog.social.dto.response.FeedResponse;
import com.petlog.social.dto.response.SearchHashtagResponse;
import com.petlog.social.dto.response.SearchResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface FeedService {
    // [리팩토링] 이미지 파일 파라미터 제거
    Long createFeed(FeedRequest.CreateFeedDto request);

    Slice<FeedResponse.GetFeedDto> getAllFeeds(Long userId, Pageable pageable);

    FeedResponse.GetFeedDto getFeed(Long feedId, Long userId);

    void updateFeed(Long feedId, FeedRequest.UpdateFeedDto request, Long userId);

    void deleteFeed(Long feedId, Long userId);

    Slice<FeedResponse.GetFeedDto> getUserFeeds(Long targetUserId, Long viewerId, Pageable pageable);

    Slice<FeedResponse.GetFeedDto> getFollowingFeeds(Long viewerId, Pageable pageable);

    // [복구] 인기 게시물 조회
    Slice<FeedResponse.GetFeedDto> getTrendingFeeds(Long viewerId, Pageable pageable);

    // [복구] 통합 검색 (유저 + 해시태그)
    SearchResponse searchAll(String query, Long viewerId, Pageable pageable);

    List<SearchHashtagResponse> searchHashtags(String query);

    // [수정] 해시태그 피드 검색 (알고리즘 정렬) - 반환 타입 일치시킴
    Slice<FeedResponse.GetFeedDto> searchFeedsByHashtagAlgorithm(String hashtag, Long userId, Pageable pageable);

    // [수정] 인기 피드 조회 (알고리즘 정렬) - 반환 타입 일치시킴
    Slice<FeedResponse.GetFeedDto> getPopularFeedsAlgorithm(Long userId, Pageable pageable);}
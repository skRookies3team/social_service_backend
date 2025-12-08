package com.example.petlog.service;

import com.example.petlog.dto.request.FeedRequest;
import com.example.petlog.dto.response.FeedResponse;
import com.example.petlog.entity.Feed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

public interface FeedService {
    // 피드 작성
    Long createFeed(FeedRequest.CreateFeedDto request, MultipartFile file);

    // [변경] 전체 피드 조회 (List -> Slice, Pageable 추가)
    Slice<FeedResponse.GetFeedDto> getAllFeeds(Long userId, Pageable pageable);

    // 특정 피드 상세 조회 (기존 동일)
    FeedResponse.GetFeedDto getFeed(Long feedId, Long userId);

    // 피드 수정 (기존 동일)
    void updateFeed(Long feedId, FeedRequest.UpdateFeedDto request, Long userId);

    // 피드 삭제 (기존 동일)
    void deleteFeed(Long feedId, Long userId);

    // [변경] 특정유저의 피드목록 조회 (List -> Slice, Pageable 추가)
    Slice<FeedResponse.GetFeedDto> getUserFeeds(Long targetUserId, Long viewerId, Pageable pageable);

    // [변경] 팔로우한 사용자 피드만 조회 (List -> Slice, Pageable 추가)
    Slice<FeedResponse.GetFeedDto> getFollowingFeeds(Long viewerId, Pageable pageable);
}
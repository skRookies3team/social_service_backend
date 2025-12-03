package com.example.petlog.service;

import com.example.petlog.dto.request.FeedRequest;
import com.example.petlog.dto.response.FeedResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FeedService {
    // 피드 작성
    Long createFeed(FeedRequest.CreateFeedDto request, MultipartFile file);

    // 전체 피드 조회 (좋아요 여부 확인을 위해 userId 추가)
    List<FeedResponse.GetFeedDto> getAllFeeds(Long userId);

    // 특정 피드 상세 조회 (좋아요 여부 확인을 위해 userId 추가)
    FeedResponse.GetFeedDto getFeed(Long feedId, Long userId);

    // 피드 수정
    void updateFeed(Long feedId, FeedRequest.UpdateFeedDto request, Long userId);

    // 피드 삭제
    void deleteFeed(Long feedId, Long userId);

    // 특정유저의 피드목록 조회
    List<FeedResponse.GetFeedDto> getUserFeeds(Long targetUserId, Long viewerId);
}
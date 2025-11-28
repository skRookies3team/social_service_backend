package com.example.petlog.service;

import com.example.petlog.dto.request.FeedRequest;
import com.example.petlog.dto.response.FeedResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FeedService {
    // 피드 작성
    Long createFeed(FeedRequest.CreateFeedDto request, MultipartFile file);

    // 전체 피드 조회
    List<FeedResponse.GetFeedDto> getAllFeeds();

    // 특정 피드 상세 조회
    FeedResponse.GetFeedDto getFeed(Long feedId);

    // 피드 수정 (본인 확인용 userId 포함)
    void updateFeed(Long feedId, FeedRequest.UpdateFeedDto request, Long userId);

    // 피드 삭제 (본인 확인용 userId 포함)
    void deleteFeed(Long feedId, Long userId);
}

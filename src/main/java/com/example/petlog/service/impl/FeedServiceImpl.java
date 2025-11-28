package com.example.petlog.service.impl; // 패키지 경로 주의

import com.example.petlog.client.PetClient;
import com.example.petlog.client.UserClient;
import com.example.petlog.dto.request.FeedRequest;
import com.example.petlog.dto.response.FeedResponse;
import com.example.petlog.entity.Feed;
import com.example.petlog.exception.BusinessException;
import com.example.petlog.exception.EntityNotFoundException;
import com.example.petlog.exception.ErrorCode;
import com.example.petlog.repository.FeedRepository;
import com.example.petlog.service.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    // Feign Clients 주입
    private final UserClient userClient;
    private final PetClient petClient;

    @Override
    @Transactional
    public Long createFeed(FeedRequest.CreateFeedDto request) {
        // 1. 외부 서비스 통신을 통한 검증 (선택적)
        // try-catch 등으로 외부 서비스 장애 시 처리 정책 필요
        // if (!userClient.checkUserExists(request.getUserId())) { ... }

        // 2. 엔티티 생성 (ID만 저장)
        Feed feed = Feed.builder()
                .userId(request.getUserId())
                .petId(request.getPetId()) // null 가능
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .build();

        return feedRepository.save(feed).getId();
    }

    @Override
    public List<FeedResponse.GetFeedDto> getAllFeeds() {
        List<Feed> feeds = feedRepository.findAllByOrderByCreatedAtDesc();

        // N+1 문제 방지를 위해 실무에서는 ID 목록으로 일괄 조회 API 등을 활용 권장
        return feeds.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public FeedResponse.GetFeedDto getFeed(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId)); // 수정된 생성자 활용

        return convertToDto(feed);
    }

    @Override
    @Transactional
    public void updateFeed(Long feedId, FeedRequest.UpdateFeedDto request, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));

        if (!feed.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FEED_UNAUTHORIZED);
        }

        feed.updateFeed(request.getContent(), request.getImageUrl());
    }

    @Override
    @Transactional
    public void deleteFeed(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));

        if (!feed.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FEED_UNAUTHORIZED);
        }

        feedRepository.delete(feed);
    }

    // 엔티티 -> DTO 변환 (외부 데이터 조합)
    private FeedResponse.GetFeedDto convertToDto(Feed feed) {
        String nickname = "Unknown";
        String petName = null;

        try {
            nickname = userClient.getNickname(feed.getUserId());
        } catch (Exception e) {
            log.error("User Service 호출 실패: {}", e.getMessage());
            // Fallback 로직 또는 에러 처리
        }

        if (feed.getPetId() != null) {
            try {
                petName = petClient.getPetName(feed.getPetId());
            } catch (Exception e) {
                log.error("Pet Service 호출 실패: {}", e.getMessage());
            }
        }

        return FeedResponse.GetFeedDto.of(feed, nickname, petName);
    }
}
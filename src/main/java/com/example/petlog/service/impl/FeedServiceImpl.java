package com.example.petlog.service.impl;

import com.example.petlog.client.PetClient;
import com.example.petlog.client.UserClient;
import com.example.petlog.dto.request.FeedRequest;
import com.example.petlog.dto.response.FeedResponse;
import com.example.petlog.entity.Feed;
import com.example.petlog.exception.BusinessException;
import com.example.petlog.exception.EntityNotFoundException;
import com.example.petlog.exception.ErrorCode;
import com.example.petlog.repository.FeedLikeRepository;
import com.example.petlog.repository.FeedRepository;
import com.example.petlog.service.FeedService;
import com.example.petlog.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository; // ✅ 좋아요 Repo 추가
    private final UserClient userClient;
    private final PetClient petClient;
    private final ImageService imageService;

    @Override
    @Transactional
    public Long createFeed(FeedRequest.CreateFeedDto request, MultipartFile file) {
        String filename = null;

        if (file != null && !file.isEmpty()) {
            filename = imageService.upload(file);
        }

        Feed feed = Feed.builder()
                .userId(request.getUserId())
                .petId(request.getPetId())
                .content(request.getContent())
                .location(request.getLocation())
                .imageUrl(filename)
                .build();

        return feedRepository.save(feed).getId();
    }

    @Override
    public List<FeedResponse.GetFeedDto> getAllFeeds(Long currentUserId) {
        return feedRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(feed -> convertToDto(feed, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public FeedResponse.GetFeedDto getFeed(Long feedId, Long currentUserId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));
        return convertToDto(feed, currentUserId);
    }

    @Override
    @Transactional
    public void updateFeed(Long feedId, FeedRequest.UpdateFeedDto request, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));

        if (!feed.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FEED_UNAUTHORIZED);
        }

        feed.updateFeed(request.getContent(), request.getImageUrl(), request.getLocation());
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

    // 엔티티 -> DTO 변환 (좋아요 정보 포함)
    private FeedResponse.GetFeedDto convertToDto(Feed feed, Long currentUserId) {
        String nickname = "Unknown";
        String petName = null;

        try {
            nickname = userClient.getNickname(feed.getUserId());
        } catch (Exception e) {
            log.warn("User Service 호출 실패: {}", e.getMessage());
        }

        if (feed.getPetId() != null) {
            try {
                petName = petClient.getPetName(feed.getPetId());
            } catch (Exception e) {
                log.warn("Pet Service 호출 실패: {}", e.getMessage());
            }
        }

        String fullImageUrl = null;
        if (feed.getImageUrl() != null) {
            if (feed.getImageUrl().startsWith("http")) {
                fullImageUrl = feed.getImageUrl();
            } else {
                fullImageUrl = imageService.getImageUrl(feed.getImageUrl());
            }
        }

        // ✅ 좋아요 정보 조회
        long likeCount = feedLikeRepository.countByFeed(feed);
        boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = feedLikeRepository.existsByFeedAndUserId(feed, currentUserId);
        }

        // DTO 생성 (파라미터 6개)
        return FeedResponse.GetFeedDto.of(feed, nickname, petName, fullImageUrl, likeCount, isLiked);
    }
}
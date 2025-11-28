package com.example.petlog.service.impl;

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
    private final UserClient userClient;
    private final PetClient petClient;

    // 이미지 처리 로직을 담당하는 서비스 주입 (LocalImageService or S3ImageService)
    private final ImageService imageService;

    @Override
    @Transactional
    public Long createFeed(FeedRequest.CreateFeedDto request, MultipartFile file) {
        String filename = null;

        // 1. 이미지 업로드 (ImageService에 위임)
        if (file != null && !file.isEmpty()) {
            filename = imageService.upload(file);
        }

        // 2. 피드 엔티티 생성 및 저장 (DB에는 파일명만 저장)
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
    public List<FeedResponse.GetFeedDto> getAllFeeds() {
        return feedRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public FeedResponse.GetFeedDto getFeed(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));
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

        // 이미지 수정 로직은 현재 요구사항에 없으므로 기존 이미지 유지
        // 필요 시 request.getImageUrl() 대신 파일 업로드 로직 추가 가능
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

    // 엔티티 -> DTO 변환 (이미지 URL 생성 포함)
    private FeedResponse.GetFeedDto convertToDto(Feed feed) {
        String nickname = "Unknown";
        String petName = null;

        // 1. User Service 통신
        try {
            nickname = userClient.getNickname(feed.getUserId());
        } catch (Exception e) {
            log.warn("User Service 호출 실패: {}", e.getMessage());
        }

        // 2. Pet Service 통신
        if (feed.getPetId() != null) {
            try {
                petName = petClient.getPetName(feed.getPetId());
            } catch (Exception e) {
                log.warn("Pet Service 호출 실패: {}", e.getMessage());
            }
        }

        // 3. 이미지 URL 생성 (ImageService에 위임)
        String fullImageUrl = null;
        if (feed.getImageUrl() != null) {
            if (feed.getImageUrl().startsWith("http")) {
                fullImageUrl = feed.getImageUrl(); // 기존 테스트 데이터 호환
            } else {
                // Local일 땐 "/images/파일.jpg", S3일 땐 "https://s3.../파일.jpg" 반환
                fullImageUrl = imageService.getImageUrl(feed.getImageUrl());
            }
        }

        return FeedResponse.GetFeedDto.of(feed, nickname, petName, fullImageUrl);
    }
}
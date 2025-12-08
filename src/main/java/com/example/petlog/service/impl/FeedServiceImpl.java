package com.example.petlog.service.impl;

import com.example.petlog.client.PetClient;
import com.example.petlog.client.UserClient;
import com.example.petlog.dto.request.FeedRequest;
import com.example.petlog.dto.response.CommentResponse;
import com.example.petlog.dto.response.FeedResponse;
import com.example.petlog.entity.Comment;
import com.example.petlog.entity.Feed;
import com.example.petlog.exception.BusinessException;
import com.example.petlog.exception.EntityNotFoundException;
import com.example.petlog.exception.ErrorCode;
import com.example.petlog.repository.CommentRepository;
import com.example.petlog.repository.FeedLikeRepository;
import com.example.petlog.repository.FeedRepository;
import com.example.petlog.service.FeedService;
import com.example.petlog.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    private final FeedLikeRepository feedLikeRepository;
    private final UserClient userClient;
    private final PetClient petClient;
    private final ImageService imageService;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public Long createFeed(FeedRequest.CreateFeedDto request, MultipartFile file) {
        String filename = null;

        // 이미지 파일이 있으면 업로드 수행
        if (file != null && !file.isEmpty()) { // 이미지서비스한테 파일 저장 시키고 저장된 파일명 filename에 저장
            filename = imageService.upload(file);
        }
        // 피드엔티티 생성 (db에 save할 객체)
        Feed feed = Feed.builder()
                .userId(request.getUserId())
                .petId(request.getPetId())
                .content(request.getContent())
                .location(request.getLocation())
                .imageUrl(filename)
                .build();
        // db 저장하고 생성된 id반환
        return feedRepository.save(feed).getId();
    }

    @Override
    public Slice<FeedResponse.GetFeedDto> getAllFeeds(Long currentUserId, Pageable pageable) {
        Slice<Feed> feedSlice = feedRepository.findAllByOrderByCreatedAtDesc(pageable);
        // map 함수를 사용하여 Slice 내부의 내용물만 DTO로 변환
        return feedSlice.map(feed -> convertToDto(feed, currentUserId));
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

    @Override
    public Slice<FeedResponse.GetFeedDto> getUserFeeds(Long targetUserId, Long viewerId, Pageable pageable) {
        Slice<Feed> feedSlice = feedRepository.findAllByUserIdOrderByCreatedAtDesc(targetUserId, pageable);
        return feedSlice.map(feed -> convertToDto(feed, viewerId));
    }

    @Override
    public Slice<FeedResponse.GetFeedDto> getFollowingFeeds(Long viewerId, Pageable pageable) {
        Slice<Feed> feedSlice = feedRepository.findAllByFollowingUsers(viewerId, pageable);
        return feedSlice.map(feed -> convertToDto(feed, viewerId));
    }

    // 엔티티 -> DTO 변환 (기존 로직 유지)
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

        long likeCount = feedLikeRepository.countByFeed(feed);
        boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = feedLikeRepository.existsByFeedAndUserId(feed, currentUserId);
        }

        Long commentCount = commentRepository.countByFeedId(feed.getId());
        List<Comment> top3Comments = commentRepository.findTop3ByFeedIdAndParentIsNullOrderByCreatedAtDesc(feed.getId());

        List<CommentResponse.CommentDto> recentComments = top3Comments.stream()
                .map(c -> CommentResponse.CommentDto.of(c, "Unknown"))
                .collect(Collectors.toList());

        return FeedResponse.GetFeedDto.of(feed, nickname, petName, fullImageUrl, likeCount, isLiked, commentCount, recentComments);
    }
}
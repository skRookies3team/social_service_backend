package com.example.petlog.service.impl;

import com.example.petlog.client.UserClient;
import com.example.petlog.dto.response.FeedLikeResponse;
import com.example.petlog.entity.Feed;
import com.example.petlog.entity.FeedLike;
import com.example.petlog.exception.EntityNotFoundException;
import com.example.petlog.repository.FeedLikeRepository;
import com.example.petlog.repository.FeedRepository;
import com.example.petlog.service.FeedLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FeedLikeServiceImpl implements FeedLikeService {

    private final FeedLikeRepository feedLikeRepository;
    private final FeedRepository feedRepository;
    private final UserClient userClient;

    @Override
    @Transactional
    public boolean toggleLike(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));

        Optional<FeedLike> existingLike = feedLikeRepository.findByFeedAndUserId(feed, userId);

        if (existingLike.isPresent()) {
            feedLikeRepository.delete(existingLike.get());
            return false;
        } else {
            feedLikeRepository.save(FeedLike.builder().feed(feed).userId(userId).build());
            return true;
        }
    }

    // 좋아요 누른 사람 목록 조회 (전체 공개)
    @Override
    public List<FeedLikeResponse.LikerDto> getLikers(Long feedId) {
        // 1. 피드 조회
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));

        // 🔥 주인 권한 체크 로직 (if !feed.getUserId().equals(userId)...) 제거

        // 2. 좋아요 목록 반환 (누구나 조회 가능)
        return feedLikeRepository.findAllByFeed(feed).stream()
                .map(like -> {
                    String nickname = "Unknown";
                    try {
                        nickname = userClient.getNickname(like.getUserId());
                    } catch (Exception e) {
                        log.warn("User Service Error: {}", e.getMessage());
                    }

                    return FeedLikeResponse.LikerDto.builder()
                            .userId(like.getUserId())
                            .nickname(nickname)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
package com.petlog.social.service.impl;

import com.petlog.social.client.UserClient;
import com.petlog.social.dto.client.UserClientResponse;
import com.petlog.social.dto.response.FeedLikeResponse;
import com.petlog.social.entity.Feed;
import com.petlog.social.entity.FeedLike;
import com.petlog.social.exception.EntityNotFoundException;
import com.petlog.social.repository.FeedLikeRepository;
import com.petlog.social.repository.FeedRepository;
import com.petlog.social.service.FeedLikeService;
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
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));

        return feedLikeRepository.findAllByFeed(feed).stream()
                .map(like -> {
                    String nickname = "Unknown";
                    try {
                        UserClientResponse userDto = userClient.getUser(like.getUserId());
                        if (userDto != null) {
                            // [수정] getNickname() -> getUsername()
                            nickname = userDto.getUsername();
                        }
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
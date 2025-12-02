package com.example.petlog.service.impl;

import com.example.petlog.entity.Feed;
import com.example.petlog.entity.FeedLike;
import com.example.petlog.exception.EntityNotFoundException;
import com.example.petlog.repository.FeedLikeRepository;
import com.example.petlog.repository.FeedRepository;
import com.example.petlog.service.FeedLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedLikeServiceImpl implements FeedLikeService {

    private final FeedLikeRepository feedLikeRepository;
    private final FeedRepository feedRepository;

    @Override
    @Transactional
    public boolean toggleLike(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));

        Optional<FeedLike> existingLike = feedLikeRepository.findByFeedAndUserId(feed, userId);

        if (existingLike.isPresent()) {
            // 이미 좋아요가 있다면 -> 삭제 (취소)
            feedLikeRepository.delete(existingLike.get());
            return false;
        } else {
            // 좋아요가 없다면 -> 저장 (등록)
            feedLikeRepository.save(FeedLike.builder()
                    .feed(feed)
                    .userId(userId)
                    .build());
            return true;
        }
    }
}
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
import org.springframework.dao.DataIntegrityViolationException;
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
    public FeedLikeResponse.ToggleLikeResponse toggleLike(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));

        Optional<FeedLike> existingLike = feedLikeRepository.findByFeedAndUserId(feed, userId);
        boolean isLiked;

        if (existingLike.isPresent()) {
            // 이미 좋아요 상태 -> 취소
            feedLikeRepository.delete(existingLike.get());
            isLiked = false;
        } else {
            // 좋아요 없음 -> 생성
            try {
                feedLikeRepository.save(FeedLike.builder().feed(feed).userId(userId).build());
                isLiked = true;
            } catch (DataIntegrityViolationException e) {
                // [방어 코드] 동시에 요청이 들어와서 DB 제약 조건(Unique)에 걸린 경우
                // 이미 좋아요가 눌린 상태로 간주하여 처리
                log.warn("좋아요 중복 요청 감지 (FeedId: {}, UserId: {})", feedId, userId);
                isLiked = true;
            }
        }

        // [중요] 변경 사항 즉시 반영 후 최신 개수 조회
        feedLikeRepository.flush();
        long currentCount = feedLikeRepository.countByFeed(feed);

        return new FeedLikeResponse.ToggleLikeResponse(isLiked, currentCount);
    }

    @Override
    public List<FeedLikeResponse.LikerDto> getLikers(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));

        return feedLikeRepository.findAllByFeed(feed).stream()
                .map(like -> {
                    String nickname = "Unknown";
                    String profileImage = null;

                    try {
                        UserClientResponse userDto = userClient.getUser(like.getUserId());
                        if (userDto != null) {
                            nickname = userDto.getUsername(); // 닉네임 필드명 확인 필요
                            // [추가] 프로필 이미지 가져오기
                            // UserClientResponse에 getProfileImage() 메소드가 있어야 합니다.
                            profileImage = userDto.getProfileImage();
                        }
                    } catch (Exception e) {
                        log.warn("User Service Error: {}", e.getMessage());
                    }

                    return FeedLikeResponse.LikerDto.builder()
                            .userId(like.getUserId())
                            .nickname(nickname)
                            .profileImage(profileImage)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
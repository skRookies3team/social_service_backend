package com.petlog.social.service.impl;

import com.petlog.social.client.PetClient;
import com.petlog.social.client.UserClient;
import com.petlog.social.dto.client.PetClientResponse;
import com.petlog.social.dto.client.UserClientResponse;
import com.petlog.social.dto.client.UserSearchListResponse;
import com.petlog.social.dto.request.FeedRequest;
import com.petlog.social.dto.response.CommentResponse;
import com.petlog.social.dto.response.FeedResponse;
import com.petlog.social.dto.response.SearchResponse;
import com.petlog.social.entity.*;
import com.petlog.social.exception.BusinessException;
import com.petlog.social.exception.EntityNotFoundException;
import com.petlog.social.exception.ErrorCode;
import com.petlog.social.repository.*;
import com.petlog.social.service.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final HashtagRepository hashtagRepository;
    private final FeedHashtagRepository feedHashtagRepository;
    private final CommentRepository commentRepository;

    // [추가] 이미지 리포지토리
    private final FeedImageRepository feedImageRepository;

    private final UserClient userClient;
    private final PetClient petClient;

    /**
     * 피드 작성 (이미지 여러 장 지원)
     */
    @Override
    @Transactional
    public Long createFeed(FeedRequest.CreateFeedDto request) {
        // 1. 피드 엔티티 생성 (이미지 제외)
        Feed feed = Feed.builder()
                .userId(request.getUserId())
                .petId(request.getPetId())
                .content(request.getContent())
                .location(request.getLocation())
                .build();

        // 2. 이미지 리스트 추가 (Feed 엔티티의 편의 메서드 사용)
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (String url : request.getImageUrls()) {
                feed.addImage(url);
            }
        }

        // 3. 저장 (Cascade 설정을 통해 FeedImage들도 함께 저장됨)
        Feed savedFeed = feedRepository.save(feed);

        // 4. 해시태그 처리
        processHashtags(savedFeed, request.getContent());

        return savedFeed.getId();
    }

    @Override
    public Slice<FeedResponse.GetFeedDto> getAllFeeds(Long currentUserId, Pageable pageable) {
        Slice<Feed> feedSlice = feedRepository.findAllByOrderByCreatedAtDesc(pageable);
        return feedSlice.map(feed -> convertToDto(feed, currentUserId));
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

    @Override
    public Slice<FeedResponse.GetFeedDto> getTrendingFeeds(Long viewerId, Pageable pageable) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        Slice<Feed> feedSlice = feedRepository.findTrendingFeeds(oneWeekAgo, pageable);
        return feedSlice.map(feed -> convertToDto(feed, viewerId));
    }

    @Override
    public SearchResponse searchAll(String query, Long viewerId, Pageable pageable) {
        List<UserClientResponse> users = new ArrayList<>();
        String hashtagKeyword = query;

        // 1. 유저 검색 (#이 없을 때 -> keyword 파라미터로 전송)
        if (!query.startsWith("#")) {
            try {
                // UserClient의 파라미터명이 keyword로 바뀌었으므로, query 값을 그대로 넘김
                UserSearchListResponse response = userClient.searchUsersWithSocial(query);

                // User Service의 응답 구조(isEmpty, users)에 맞춰 데이터 추출
                if (response != null && !response.isEmpty() && response.getUsers() != null) {
                    users = response.getUsers();
                }
            } catch (Exception e) {
                log.error("User Search Failed: {}", e.getMessage());
            }
        } else {
            hashtagKeyword = query.substring(1); // # 제거
        }

        // 2. 해시태그 피드 검색 (기존 유지)
        Slice<Feed> feeds = feedRepository.findByHashtag(hashtagKeyword, pageable);
        Slice<FeedResponse.GetFeedDto> feedDtos = feeds.map(feed -> convertToDto(feed, viewerId));

        return SearchResponse.of(users, feedDtos);
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

        // [수정] 내용 및 이미지 리스트 업데이트
        feed.updateContent(request.getContent(), request.getLocation());
        feed.updateImages(request.getImageUrls());
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

    // --- Private Helper Methods ---

    private void processHashtags(Feed feed, String content) {
        if (content == null || content.isEmpty()) return;
        Pattern pattern = Pattern.compile("#([0-9a-zA-Z가-힣_]+)");
        Matcher matcher = pattern.matcher(content);
        Set<String> tags = new HashSet<>();
        while (matcher.find()) tags.add(matcher.group(1));

        for (String tagName : tags) {
            Hashtag hashtag = hashtagRepository.findByName(tagName)
                    .orElseGet(() -> hashtagRepository.save(new Hashtag(tagName)));
            feedHashtagRepository.save(FeedHashtag.builder().feed(feed).hashtag(hashtag).build());
        }
    }

    private FeedResponse.GetFeedDto convertToDto(Feed feed, Long currentUserId) {
        String nickname = "Unknown";
        String profileImage = null;
        String socialId = "";

        // User Service 호출
        try {
            UserClientResponse user = userClient.getUser(feed.getUserId());
            if (user != null) {
                nickname = user.getUsername();
                profileImage = user.getProfileImage();
                socialId = user.getSocial();
            }
        } catch (Exception e) {
            log.warn("User fetch failed: {}", e.getMessage());
        }

        String petName = null;
        if (feed.getPetId() != null) {
            try {
                PetClientResponse pet = petClient.getPet(feed.getPetId());
                if (pet != null) petName = pet.getPetName();
            } catch (Exception e) {}
        }

        long likeCount = feedLikeRepository.countByFeed(feed);
        boolean isLiked = currentUserId != null && feedLikeRepository.existsByFeedAndUserId(feed, currentUserId);
        Long commentCount = commentRepository.countByFeedId(feed.getId());

        List<Comment> top3Comments = commentRepository.findTop3ByFeedIdAndParentIsNullOrderByCreatedAtDesc(feed.getId());
        List<CommentResponse.CommentDto> recentComments = top3Comments.stream()
                .map(c -> CommentResponse.CommentDto.of(c, null))
                .collect(Collectors.toList());

        List<String> hashtags = feed.getFeedHashtags().stream()
                .map(fh -> fh.getHashtag().getName())
                .collect(Collectors.toList());

        // [변경] FeedImage 엔티티 리스트 -> String URL 리스트로 변환
        List<String> imageUrls = feed.getFeedImages().stream()
                .map(FeedImage::getImageUrl)
                .collect(Collectors.toList());

        return FeedResponse.GetFeedDto.builder()
                .feedId(feed.getId())
                .writerId(feed.getUserId())
                .writerNickname(nickname)
                .writerProfileImage(profileImage)
                .writerSocialId(socialId)
                .petName(petName)
                .content(feed.getContent())
                .imageUrls(imageUrls) // [변경] List<String>
                .location(feed.getLocation())
                .likeCount(likeCount)
                .isLiked(isLiked)
                .commentCount(commentCount)
                .recentComments(recentComments)
                .hashtags(hashtags)
                .createdAt(feed.getCreatedAt())
                .build();
    }
}
package com.petlog.social.service.impl;

import com.petlog.social.client.PetClient;
import com.petlog.social.client.UserClient;
import com.petlog.social.dto.client.PetClientResponse;
import com.petlog.social.dto.client.UserClientResponse;
import com.petlog.social.dto.client.UserSearchListResponse;
import com.petlog.social.dto.request.FeedRequest;
import com.petlog.social.dto.response.CommentResponse;
import com.petlog.social.dto.response.FeedResponse;
import com.petlog.social.dto.response.SearchHashtagResponse;
import com.petlog.social.dto.response.SearchResponse;
import com.petlog.social.repository.BlockRepository;
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
    private final BlockRepository blockRepository;

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

    /**
     * 전체 피드 조회 (차단 필터링 적용)
     */
    @Override
    public Slice<FeedResponse.GetFeedDto> getAllFeeds(Long currentUserId, Pageable pageable) {
        // 1. 내가 차단한 유저 ID 목록 조회
        List<Long> blockedUserIds = blockRepository.findBlockedIdsByBlockerId(currentUserId);

        Slice<Feed> feedSlice;
        if (blockedUserIds.isEmpty()) {
            // 차단한 사람이 없으면 기존대로 전체 조회
            feedSlice = feedRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            // 차단한 유저(blockedUserIds)를 제외하고 조회
            // [주의] FeedRepository에 해당 메서드가 정의되어 있어야 합니다.
            feedSlice = feedRepository.findAllByUserIdNotInOrderByCreatedAtDesc(blockedUserIds, pageable);
        }

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

        // 차단 목록 조회
        List<Long> blockedUserIds = blockRepository.findBlockedIdsByBlockerId(viewerId);

        Slice<Feed> feedSlice;
        if (blockedUserIds.isEmpty()) {
            // 차단한 사람이 없으면 기본 메서드
            feedSlice = feedRepository.findTrendingFeeds(oneWeekAgo, pageable);
        } else {
            // 차단한 사람이 있으면 필터링 메서드 호출
            feedSlice = feedRepository.findTrendingFeedsBlocked(oneWeekAgo, blockedUserIds, pageable);
        }

        return feedSlice.map(feed -> convertToDto(feed, viewerId));
    }

    @Override
    public SearchResponse searchAll(String query, Long viewerId, Pageable pageable) {
        List<UserClientResponse> users = new ArrayList<>();
        String hashtagKeyword = query;

        // 1. 유저 검색 (#이 없을 때만 수행)
        if (!query.startsWith("#")) {
            try {
                UserSearchListResponse response = userClient.searchUsersWithSocial(query);
                if (response != null && !response.isEmpty() && response.getUsers() != null) {
                    users = response.getUsers();
                }
            } catch (Exception e) {
                log.error("User Search Failed: {}", e.getMessage());
            }
        } else {
            // #으로 시작하면 # 제거 후 키워드로 사용
            hashtagKeyword = query.substring(1);
        }

        // 2. 해시태그 피드 검색 (기존 로직)
        Slice<Feed> feeds = feedRepository.findByHashtag(hashtagKeyword, pageable);
        Slice<FeedResponse.GetFeedDto> feedDtos = feeds.map(feed -> convertToDto(feed, viewerId));

        // 3. [추가] 해시태그 자체 검색 (자동완성/추천용)
        // 검색어(hashtagKeyword)로 해시태그 목록도 조회해서 같이 내려줍니다.
        List<SearchHashtagResponse> hashtags = searchHashtags(hashtagKeyword);

        // [수정] 인자 3개 (users, feeds, hashtags) 전달
        return SearchResponse.of(users, feedDtos, hashtags);
    }

    @Override
    public List<SearchHashtagResponse> searchHashtags(String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }
        // 검색어에서 '#' 제거 (프론트에서 처리해서 보내더라도 안전하게 한 번 더 처리)
        String cleanQuery = query.replace("#", "");

        // 상위 10개만 검색 (Pageable.ofSize(10))
        return hashtagRepository.searchHashtagsByName(cleanQuery, Pageable.ofSize(10));
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
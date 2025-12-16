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
import com.petlog.social.entity.Comment;
import com.petlog.social.entity.Feed;
import com.petlog.social.entity.FeedHashtag;
import com.petlog.social.entity.Hashtag;
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

/**
 * 피드(게시물) 관련 비즈니스 로직을 처리하는 구현체입니다.
 * MSA 환경을 고려하여 User Service, Pet Service와 통신합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 사용 (조회 성능 최적화)
@Slf4j
public class FeedServiceImpl implements FeedService {

    // --- Repository (DB 접근) ---
    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final HashtagRepository hashtagRepository;
    private final FeedHashtagRepository feedHashtagRepository;
    private final CommentRepository commentRepository;

    // --- Feign Client (외부 서비스 통신) ---
    private final UserClient userClient;
    private final PetClient petClient;

    /**
     * 피드 작성
     * 기존의 이미지 업로드 로직을 제거하고, 클라이언트(혹은 User Service)로부터 받은 이미지 URL을 바로 저장합니다.
     */
    @Override
    @Transactional // 쓰기 작업이므로 Transactional 필수
    public Long createFeed(FeedRequest.CreateFeedDto request) {
        // 1. 피드 엔티티 생성 (Builder 패턴 사용)
        Feed feed = Feed.builder()
                .userId(request.getUserId())
                .petId(request.getPetId())
                .content(request.getContent())
                .location(request.getLocation())
                .imageUrl(request.getImageUrl()) // 이미 업로드된 URL을 그대로 저장
                .build();

        // 2. DB 저장
        Feed savedFeed = feedRepository.save(feed);

        // 3. 본문 내용에서 해시태그(#) 추출 및 저장 로직 수행
        processHashtags(savedFeed, request.getContent());

        return savedFeed.getId();
    }

    /**
     * 전체 피드 조회 (메인 탐색 탭)
     * 무한 스크롤을 위해 Page 대신 Slice를 사용합니다. (Count 쿼리 생략하여 성능 유리)
     */
    @Override
    public Slice<FeedResponse.GetFeedDto> getAllFeeds(Long currentUserId, Pageable pageable) {
        Slice<Feed> feedSlice = feedRepository.findAllByOrderByCreatedAtDesc(pageable);
        // 엔티티 목록을 DTO 목록으로 변환 (User 정보 매핑 포함)
        return feedSlice.map(feed -> convertToDto(feed, currentUserId));
    }

    /**
     * 특정 유저의 피드 모아보기 (마이페이지/상대방 프로필)
     */
    @Override
    public Slice<FeedResponse.GetFeedDto> getUserFeeds(Long targetUserId, Long viewerId, Pageable pageable) {
        Slice<Feed> feedSlice = feedRepository.findAllByUserIdOrderByCreatedAtDesc(targetUserId, pageable);
        return feedSlice.map(feed -> convertToDto(feed, viewerId));
    }

    /**
     * 팔로잉 뉴스피드 (내가 팔로우한 사람들의 글만 보기)
     */
    @Override
    public Slice<FeedResponse.GetFeedDto> getFollowingFeeds(Long viewerId, Pageable pageable) {
        Slice<Feed> feedSlice = feedRepository.findAllByFollowingUsers(viewerId, pageable);
        return feedSlice.map(feed -> convertToDto(feed, viewerId));
    }

    /**
     * 인기 게시물 조회
     * 최근 7일 이내의 게시물 중 '좋아요'가 많은 순서대로 조회합니다.
     */
    @Override
    public Slice<FeedResponse.GetFeedDto> getTrendingFeeds(Long viewerId, Pageable pageable) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7); // 일주일 전 날짜 계산
        Slice<Feed> feedSlice = feedRepository.findTrendingFeeds(oneWeekAgo, pageable);
        return feedSlice.map(feed -> convertToDto(feed, viewerId));
    }

    /**
     * 피드 상세 조회 (단건)
     */
    @Override
    public FeedResponse.GetFeedDto getFeed(Long feedId, Long currentUserId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));
        return convertToDto(feed, currentUserId);
    }

    /**
     * 피드 수정
     * 작성자 본인인지 확인 후 내용을 수정합니다.
     */
    @Override
    @Transactional
    public void updateFeed(Long feedId, FeedRequest.UpdateFeedDto request, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));

        // 권한 체크
        if (!feed.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FEED_UNAUTHORIZED);
        }

        // 더티 체킹(Dirty Checking)으로 업데이트 수행
        feed.updateFeed(request.getContent(), request.getImageUrl(), request.getLocation());
    }

    /**
     * 통합 검색 (유저 + 해시태그)
     * 검색어가 '#'으로 시작하면 해시태그만, 아니면 유저 검색도 같이 수행합니다.
     */
    @Override
    public SearchResponse searchAll(String query, Long viewerId, Pageable pageable) {
        List<UserClientResponse> users = new ArrayList<>();
        String hashtagKeyword = query;

        // 1. 유저 검색 시도 (검색어가 #으로 시작하지 않을 때만)
        if (!query.startsWith("#")) {
            try {
                // User Service에 검색 요청
                UserSearchListResponse response = userClient.searchUsersWithSocial(query);
                if (response != null && response.getUsers() != null) {
                    users = response.getUsers();
                }
            } catch (Exception e) {
                // 외부 서비스 에러가 나도 피드 검색은 작동하도록 로그만 남김
                log.error("User Search Failed: {}", e.getMessage());
            }
        } else {
            hashtagKeyword = query.substring(1); // 앞에 '#' 문자 제거
        }

        // 2. 해시태그 기반 피드 검색
        Slice<Feed> feeds = feedRepository.findByHashtag(hashtagKeyword, pageable);
        Slice<FeedResponse.GetFeedDto> feedDtos = feeds.map(feed -> convertToDto(feed, viewerId));

        // 3. 결과 합치기
        return SearchResponse.of(users, feedDtos);
    }

    /**
     * 피드 삭제
     */
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

    // =================================================================================
    // Private Helper Methods (내부 로직 분리)
    // =================================================================================

    /**
     * 해시태그 추출 및 저장
     * 정규식을 사용하여 본문에서 태그를 추출하고, Hashtag 테이블과 매핑 테이블에 저장합니다.
     */
    private void processHashtags(Feed feed, String content) {
        if (content == null || content.isEmpty()) return;

        // 정규식: # 뒤에 한글, 영어, 숫자, 언더바(_)가 오는 패턴
        Pattern pattern = Pattern.compile("#([0-9a-zA-Z가-힣_]+)");
        Matcher matcher = pattern.matcher(content);
        Set<String> tags = new HashSet<>();

        while (matcher.find()) {
            tags.add(matcher.group(1)); // 중복 제거를 위해 Set 사용
        }

        for (String tagName : tags) {
            // 태그가 존재하면 가져오고, 없으면 새로 저장
            Hashtag hashtag = hashtagRepository.findByName(tagName)
                    .orElseGet(() -> hashtagRepository.save(new Hashtag(tagName)));

            // 피드-해시태그 연결 정보 저장
            feedHashtagRepository.save(FeedHashtag.builder().feed(feed).hashtag(hashtag).build());
        }
    }

    /**
     * 엔티티를 DTO로 변환 (User Service & Pet Service 연동 핵심 로직)
     * 피드 정보뿐만 아니라 작성자 정보(닉네임, 프사 등)와 펫 정보를 외부 서비스에서 가져와 조립합니다.
     */
    private FeedResponse.GetFeedDto convertToDto(Feed feed, Long currentUserId) {
        // 기본값 설정 (외부 서비스 장애 대비)
        String writerNickname = "알 수 없음";
        String writerProfileImage = null;
        String writerSocialId = "";

        // 1. User Service 호출 (작성자 상세 정보)
        try {
            UserClientResponse userDto = userClient.getUser(feed.getUserId());
            if (userDto != null) {
                writerNickname = userDto.getUsername();
                writerProfileImage = userDto.getProfileImage();
                writerSocialId = userDto.getSocial();
            }
        } catch (Exception e) {
            // User Service가 죽어도 피드 내용은 보여야 하므로 예외를 던지지 않고 로그만 남김
            log.warn("User Service 호출 실패 (User ID: {}): {}", feed.getUserId(), e.getMessage());
        }

        // 2. Pet Service 호출 (반려동물 이름)
        String petName = null;
        if (feed.getPetId() != null) {
            try {
                PetClientResponse petDto = petClient.getPet(feed.getPetId());
                if (petDto != null) {
                    petName = petDto.getPetName();
                }
            } catch (Exception e) {
                log.warn("Pet Service 호출 실패 (Pet ID: {}): {}", feed.getPetId(), e.getMessage());
            }
        }

        // 3. 좋아요 수 및 내가 좋아요 눌렀는지 확인
        long likeCount = feedLikeRepository.countByFeed(feed);
        boolean isLiked = currentUserId != null && feedLikeRepository.existsByFeedAndUserId(feed, currentUserId);

        // 4. 댓글 수 및 최신 댓글 3개 미리보기
        Long commentCount = commentRepository.countByFeedId(feed.getId());
        List<Comment> top3Comments = commentRepository.findTop3ByFeedIdAndParentIsNullOrderByCreatedAtDesc(feed.getId());

        List<CommentResponse.CommentDto> recentComments = top3Comments.stream()
                .map(c -> CommentResponse.CommentDto.of(c, null)) // 미리보기에서는 성능상 유저 정보 조회를 생략
                .collect(Collectors.toList());

        // 5. 해시태그 목록 변환
        List<String> hashtags = feed.getFeedHashtags().stream()
                .map(fh -> fh.getHashtag().getName())
                .collect(Collectors.toList());

        // 6. 최종 DTO 빌드
        return FeedResponse.GetFeedDto.builder()
                .feedId(feed.getId())
                .writerId(feed.getUserId())
                .writerSocialId(writerSocialId)       // User Service에서 가져온 소셜 ID
                .writerNickname(writerNickname)       // User Service에서 가져온 닉네임
                .writerProfileImage(writerProfileImage) // User Service에서 가져온 프로필 사진
                .petName(petName)
                .content(feed.getContent())
                .location(feed.getLocation())
                .imageUrl(feed.getImageUrl())
                .likeCount(likeCount)
                .isLiked(isLiked)
                .commentCount(commentCount)
                .recentComments(recentComments)
                .hashtags(hashtags)
                .createdAt(feed.getCreatedAt())
                .build();
    }
}
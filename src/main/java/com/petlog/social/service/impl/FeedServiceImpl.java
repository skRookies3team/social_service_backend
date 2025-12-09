package com.petlog.social.service.impl;

import com.petlog.social.client.PetClient;
import com.petlog.social.client.UserClient;
import com.petlog.social.dto.client.PetClientResponse;
import com.petlog.social.dto.client.UserClientResponse;
import com.petlog.social.dto.request.FeedRequest;
import com.petlog.social.dto.response.CommentResponse;
import com.petlog.social.dto.response.FeedResponse;
import com.petlog.social.entity.Comment;
import com.petlog.social.entity.Feed;
import com.petlog.social.entity.FeedHashtag;
import com.petlog.social.entity.Hashtag;
import com.petlog.social.exception.BusinessException;
import com.petlog.social.exception.EntityNotFoundException;
import com.petlog.social.exception.ErrorCode;
import com.petlog.social.repository.*;
import com.petlog.social.service.FeedService;
import com.petlog.social.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 설정 (성능 최적화)
@Slf4j
public class FeedServiceImpl implements FeedService {

    // 필요한 리포지토리랑 서비스들 다 가져오기
    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final HashtagRepository hashtagRepository;
    private final FeedHashtagRepository feedHashtagRepository;
    private final UserClient userClient;
    private final PetClient petClient;
    private final ImageService imageService;
    private final CommentRepository commentRepository;

    /**
     * 피드 작성
     * 이미지 업로드 -> 피드 저장 -> 해시태그 처리 순서로 진행
     */
    @Override
    @Transactional // 쓰기 작업이니까 트랜잭션 필수
    public Long createFeed(FeedRequest.CreateFeedDto request, MultipartFile file) {
        String filename = null;

        // 1. 이미지가 있으면 먼저 업로드해서 파일명 받아옴
        if (file != null && !file.isEmpty()) {
            filename = imageService.upload(file);
        }

        // 2. 피드 엔티티 만들어서
        Feed feed = Feed.builder()
                .userId(request.getUserId())
                .petId(request.getPetId())
                .content(request.getContent())
                .location(request.getLocation())
                .imageUrl(filename)
                .build();

        // 3. 일단 DB에 저장 (그래야 feedId가 생김)
        Feed savedFeed = feedRepository.save(feed);

        // 4. 본문에서 해시태그(#) 뽑아서 따로 저장
        processHashtags(savedFeed, request.getContent());

        return savedFeed.getId();
    }

    /**
     * [내부 메서드] 해시태그 추출 및 저장 로직
     * 본문 내용을 싹 훑어서 #태그 찾아내고 저장함
     */
    private void processHashtags(Feed feed, String content) {
        if (content == null || content.isEmpty()) return;

        // 정규식: # 뒤에 오는 한글, 영어, 숫자, 언더바(_) 잡기
        Pattern pattern = Pattern.compile("#([0-9a-zA-Z가-힣_]+)");
        Matcher matcher = pattern.matcher(content);

        // 중복 태그 방지용 Set (한 게시글에 #강아지 #강아지 두 번 써도 하나만 저장되게)
        Set<String> tags = new HashSet<>();
        while (matcher.find()) {
            tags.add(matcher.group(1));
        }

        // 추출한 태그들 하나씩 돌면서 저장
        for (String tagName : tags) {
            // 이미 있는 태그면 찾아오고, 없으면 새로 만듬 (Hashtag 테이블)
            Hashtag hashtag = hashtagRepository.findByName(tagName)
                    .orElseGet(() -> hashtagRepository.save(new Hashtag(tagName)));

            // 피드랑 태그 연결해주는 테이블에 저장 (FeedHashtag 테이블)
            feedHashtagRepository.save(FeedHashtag.builder()
                    .feed(feed)
                    .hashtag(hashtag)
                    .build());
        }
    }

    /**
     * 전체 피드 조회 (무한 스크롤)
     * Slice를 써서 다음 페이지가 있는지 없는지만 체크함 (성능 굿)
     */
    @Override
    public Slice<FeedResponse.GetFeedDto> getAllFeeds(Long currentUserId, Pageable pageable) {
        // DB에서 Slice로 긁어오기
        Slice<Feed> feedSlice = feedRepository.findAllByOrderByCreatedAtDesc(pageable);

        // 엔티티를 DTO로 싹 변환해서 반환
        return feedSlice.map(feed -> convertToDto(feed, currentUserId));
    }

    /**
     * 특정 유저 피드 조회 (마이페이지용)
     */
    @Override
    public Slice<FeedResponse.GetFeedDto> getUserFeeds(Long targetUserId, Long viewerId, Pageable pageable) {
        Slice<Feed> feedSlice = feedRepository.findAllByUserIdOrderByCreatedAtDesc(targetUserId, pageable);
        return feedSlice.map(feed -> convertToDto(feed, viewerId));
    }

    /**
     * 팔로우한 사람 피드만 모아보기
     */
    @Override
    public Slice<FeedResponse.GetFeedDto> getFollowingFeeds(Long viewerId, Pageable pageable) {
        Slice<Feed> feedSlice = feedRepository.findAllByFollowingUsers(viewerId, pageable);
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
     */
    @Override
    @Transactional
    public void updateFeed(Long feedId, FeedRequest.UpdateFeedDto request, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));

        // 작성자 본인 맞는지 확인
        if (!feed.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FEED_UNAUTHORIZED);
        }

        // 내용 수정 (해시태그 수정은 복잡해서 일단 패스, 나중에 구현 필요하면 말해줘요)
        feed.updateFeed(request.getContent(), request.getImageUrl(), request.getLocation());
    }

    /**
     * 피드 삭제
     */
    @Override
    @Transactional
    public void deleteFeed(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));

        // 역시 본인 확인 필수
        if (!feed.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FEED_UNAUTHORIZED);
        }
        feedRepository.delete(feed);
    }

    /**
     * [Helper] Feed 엔티티를 응답용 DTO로 변환하는 메서드
     * 여기서 여기저기 서비스 찔러서 데이터 조합함 (유저, 펫, 좋아요, 댓글 등)
     */
    private FeedResponse.GetFeedDto convertToDto(Feed feed, Long currentUserId) {
        String nickname = "Unknown";
        String petName = null;

        // 1. 유저 서비스 호출 수정
        try {
            // [변경] getUser() 호출 후 DTO에서 닉네임 꺼내기
            UserClientResponse userDto = userClient.getUser(feed.getUserId());
            if (userDto != null) {
                nickname = userDto.getNickname();
            }
        } catch (Exception e) {
            log.warn("유저 서비스 호출 실패 (User ID: {}): {}", feed.getUserId(), e.getMessage());
        }

        // 2. 펫 서비스 호출 수정
        if (feed.getPetId() != null) {
            try {
                // [변경] getPet() 호출 후 DTO에서 펫 이름 꺼내기
                PetClientResponse petDto = petClient.getPet(feed.getPetId());
                if (petDto != null) {
                    petName = petDto.getPetName();
                }
            } catch (Exception e) {
                log.warn("펫 서비스 호출 실패 (Pet ID: {}): {}", feed.getPetId(), e.getMessage());
            }
        }

        // 3. 이미지 URL 완성하기 (S3면 그대로, 로컬이면 경로 붙여서)
        String fullImageUrl = null;
        if (feed.getImageUrl() != null) {
            if (feed.getImageUrl().startsWith("http")) {
                fullImageUrl = feed.getImageUrl();
            } else {
                fullImageUrl = imageService.getImageUrl(feed.getImageUrl());
            }
        }

        // 4. 좋아요 관련 정보 채우기
        long likeCount = feedLikeRepository.countByFeed(feed); // 총 몇 개인지
        boolean isLiked = false;
        if (currentUserId != null) {
            // 내가 눌렀는지 확인
            isLiked = feedLikeRepository.existsByFeedAndUserId(feed, currentUserId);
        }

        // 5. 댓글 정보 (총 개수 + 최신 3개 미리보기)
        Long commentCount = commentRepository.countByFeedId(feed.getId());
        List<Comment> top3Comments = commentRepository.findTop3ByFeedIdAndParentIsNullOrderByCreatedAtDesc(feed.getId());

        List<CommentResponse.CommentDto> recentComments = top3Comments.stream()
                .map(c -> CommentResponse.CommentDto.of(c, "Unknown")) // 대댓글 닉네임은 일단 생략
                .collect(Collectors.toList());

        // 6. 이 피드에 달린 해시태그들 싹 긁어오기 (String 리스트로 변환)
        List<String> hashtags = feed.getFeedHashtags().stream()
                .map(fh -> fh.getHashtag().getName())
                .collect(Collectors.toList());

        // DTO에 다 때려박고 반환
        return FeedResponse.GetFeedDto.of(
                feed, nickname, petName, fullImageUrl,
                likeCount, isLiked, commentCount, recentComments,
                hashtags // 새로 추가된 부분
        );
    }
}
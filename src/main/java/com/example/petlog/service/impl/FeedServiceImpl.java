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
    public List<FeedResponse.GetFeedDto> getAllFeeds(Long currentUserId) {
        // db에서 최신순으로 모든 피드 가져오기
        return feedRepository.findAllByOrderByCreatedAtDesc().stream()
                // 각 피드를 response dto로 변환
                .map(feed -> convertToDto(feed, currentUserId))
                .collect(Collectors.toList());
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

    // 엔티티 -> DTO 변환
    private FeedResponse.GetFeedDto convertToDto(Feed feed, Long currentUserId) {
        String nickname = "Unknown";
        String petName = null;
        // 1. 유저서비스에서 닉네임 가져옴
        try {
            nickname = userClient.getNickname(feed.getUserId());
        } catch (Exception e) {
            log.warn("User Service 호출 실패: {}", e.getMessage());
        }
        // 2. 펫서비스에서 펫이름 가져옴 근데 유저랑 펫서비스가 회원으로 통합되서 수정예정
        if (feed.getPetId() != null) {
            try {
                petName = petClient.getPetName(feed.getPetId());
            } catch (Exception e) {
                log.warn("Pet Service 호출 실패: {}", e.getMessage());
            }
        }

        // 3. 이미지 주소 만들기
        String fullImageUrl = null;
        if (feed.getImageUrl() != null) {
            if (feed.getImageUrl().startsWith("http")) {
                fullImageUrl = feed.getImageUrl();
            } else {
                fullImageUrl = imageService.getImageUrl(feed.getImageUrl());
            }
        }

        // 4. 좋아요 정보 채우기 count로 좋아요수 세고 좋아요눌렀는지 확인까지
        long likeCount = feedLikeRepository.countByFeed(feed);
        boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = feedLikeRepository.existsByFeedAndUserId(feed, currentUserId);
        }

        // 댓글 미리보기 데이터 조회
        Long commentCount = commentRepository.countByFeedId(feed.getId());
        List<Comment> top3Comments = commentRepository.findTop3ByFeedIdAndParentIsNullOrderByCreatedAtDesc(feed.getId());

        // Comment 엔티티 -> DTO 변환 (간단히)
        List<CommentResponse.CommentDto> recentComments = top3Comments.stream()
                .map(c -> CommentResponse.CommentDto.of(c, "Unknown")) // 닉네임 조회 로직 생략
                .collect(Collectors.toList());

        // DTO 생성 (파라미터 6개)
        return FeedResponse.GetFeedDto.of(feed, nickname, petName, fullImageUrl, likeCount, isLiked, commentCount, recentComments);
    }

    @Override
    public List<FeedResponse.GetFeedDto> getUserFeeds(Long targetUserId, Long viewerId) {
        // 1. 해당 유저가 쓴 글만 DB에서 가져옴
        List<Feed> userFeeds = feedRepository.findAllByUserIdOrderByCreatedAtDesc(targetUserId);

        // 2. DTO로 변환 (viewerId를 넘겨서 내가 좋아요 눌렀는지도 확인 가능하게 함)
        return userFeeds.stream()
                .map(feed -> convertToDto(feed, viewerId))
                .collect(Collectors.toList());
    }
}
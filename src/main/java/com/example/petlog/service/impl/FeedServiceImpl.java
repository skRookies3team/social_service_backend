package com.example.petlog.service;

import com.example.petlog.dto.request.FeedRequest;
import com.example.petlog.dto.response.FeedResponse;
import com.example.petlog.entity.Feed;
import com.example.petlog.entity.Pet;
import com.example.petlog.entity.User;
import com.example.petlog.exception.BusinessException;
import com.example.petlog.exception.EntityNotFoundException;
import com.example.petlog.exception.ErrorCode;
import com.example.petlog.repository.FeedRepository;
import com.example.petlog.repository.PetRepository;
import com.example.petlog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 사용
public class FeedServiceImpl implements com.example.petlog.service.FeedService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;

    // 피드 작성 로직
    @Override
    @Transactional // 쓰기 작업이므로 readOnly 해제
    public Long createFeed(FeedRequest.CreateFeedDto request) {
        // 작성자 조회, 없으면 예외 발생
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 반려동물 조회 (선택 사항)
        Pet pet = null;
        if (request.getPetId() != null) {
            pet = petRepository.findById(request.getPetId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PET_NOT_FOUND));
        }

        // 엔티티 생성
        Feed feed = Feed.builder()
                .user(user)
                .pet(pet)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .build();

        // 저장 후 ID 반환
        return feedRepository.save(feed).getId();
    }

    // 전체 피드 조회 로직
    @Override
    public List<FeedResponse.GetFeedDto> getAllFeeds() {
        // 최신순으로 조회 후 DTO로 변환하여 반환
        return feedRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(FeedResponse.GetFeedDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 피드 상세 조회 로직
    @Override
    public FeedResponse.GetFeedDto getFeed(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FEED_NOT_FOUND));
        return FeedResponse.GetFeedDto.fromEntity(feed);
    }

    // 피드 수정 로직
    @Override
    @Transactional
    public void updateFeed(Long feedId, FeedRequest.UpdateFeedDto request, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FEED_NOT_FOUND));

        // 작성자 본인인지 확인
        if (!feed.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FEED_UNAUTHORIZED);
        }

        // 내용 수정 (Dirty Checking으로 자동 저장)
        feed.updateFeed(request.getContent(), request.getImageUrl());
    }

    // 피드 삭제 로직
    @Override
    @Transactional
    public void deleteFeed(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FEED_NOT_FOUND));

        // 작성자 본인인지 확인
        if (!feed.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FEED_UNAUTHORIZED);
        }

        feedRepository.delete(feed);
    }
}
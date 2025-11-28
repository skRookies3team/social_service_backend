package com.example.petlog.service.impl;

import com.example.petlog.client.PetClient;
import com.example.petlog.client.UserClient;
import com.example.petlog.dto.request.FeedRequest;
import com.example.petlog.dto.response.FeedResponse;
import com.example.petlog.entity.Feed;
import com.example.petlog.exception.BusinessException;
import com.example.petlog.exception.EntityNotFoundException;
import com.example.petlog.exception.ErrorCode;
import com.example.petlog.repository.FeedRepository;
import com.example.petlog.service.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final UserClient userClient;
    private final PetClient petClient;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    @Override
    @Transactional
    public Long createFeed(FeedRequest.CreateFeedDto request, MultipartFile file) {
        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            try {
                // uploads 폴더가 없으면 생성
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String savedFilename = UUID.randomUUID().toString() + extension;

                Path path = Paths.get(uploadDir + savedFilename);
                Files.copy(file.getInputStream(), path);

                imageUrl = "/api/images/view/" + savedFilename;

            } catch (IOException e) {
                log.error("File upload failed", e);
                throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }

        Feed feed = Feed.builder()
                .userId(request.getUserId())
                .petId(request.getPetId())
                .content(request.getContent())
                .location(request.getLocation())
                .imageUrl(imageUrl) // 저장된 이미지 URL
                .build();

        return feedRepository.save(feed).getId();
    }

    @Override
    public List<FeedResponse.GetFeedDto> getAllFeeds() {
        List<Feed> feeds = feedRepository.findAllByOrderByCreatedAtDesc();
        return feeds.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public FeedResponse.GetFeedDto getFeed(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));
        return convertToDto(feed);
    }

    @Override
    @Transactional
    public void updateFeed(Long feedId, FeedRequest.UpdateFeedDto request, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed", feedId));

        if (!feed.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FEED_UNAUTHORIZED);
        }

        // 이미지 파일 수정 로직은 이번 요구사항에 없으므로 기존 imageUrl과 location만 업데이트
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
        // 실제 파일 삭제 로직 추가 (선택)
        // if (feed.getImageUrl() != null) { ... }
        feedRepository.delete(feed);
    }

    private FeedResponse.GetFeedDto convertToDto(Feed feed) {
        String nickname = "Unknown";
        String petName = null;

        try {
            nickname = userClient.getNickname(feed.getUserId());
        } catch (Exception e) {
            log.error("User Service 호출 실패: {}", e.getMessage());
        }

        if (feed.getPetId() != null) {
            try {
                petName = petClient.getPetName(feed.getPetId());
            } catch (Exception e) {
                log.error("Pet Service 호출 실패: {}", e.getMessage());
            }
        }

        return FeedResponse.GetFeedDto.of(feed, nickname, petName);
    }
}

package com.petlog.social.service.impl;

import com.petlog.social.client.UserClient;
import com.petlog.social.dto.client.UserClientResponse;
import com.petlog.social.dto.response.FollowListResponse;
import com.petlog.social.dto.response.FollowStatResponse;
import com.petlog.social.entity.Follow;
import com.petlog.social.exception.BusinessException;
import com.petlog.social.exception.ErrorCode;
import com.petlog.social.repository.FollowRepository;
import com.petlog.social.service.FollowService;
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
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserClient userClient;

    @Override
    @Transactional
    public boolean toggleFollow(Long followerId, Long targetId) {
        // 자기 자신 팔로우 방지
        if (followerId.equals(targetId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "자기 자신을 팔로우할 수 없습니다.");
        }

        // 이미 팔로우 중인지 확인
        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowingId(followerId, targetId);

        if (existingFollow.isPresent()) {
            // 이미 했다면 -> 취소 (언팔로우)
            followRepository.delete(existingFollow.get());
            return false;
        } else {
            // 안 했다면 -> 저장 (팔로우)
            followRepository.save(Follow.builder()
                    .followerId(followerId)
                    .followingId(targetId)
                    .build());
            return true;
        }
    }

    @Override
    public FollowStatResponse getFollowStats(Long userId) {
        long followingCount = followRepository.countByFollowerId(userId);
        long followerCount = followRepository.countByFollowingId(userId);

        return FollowStatResponse.builder()
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }

    @Override
    public List<FollowListResponse> getFollowers(Long userId) {
        // 나(userId)를 following 하고 있는 사람들 조회
        return followRepository.findAllByFollowingId(userId).stream()
                .map(follow -> {
                    Long targetId = follow.getFollowerId();
                    return FollowListResponse.builder()
                            .userId(targetId)
                            .nickname(getUserNickname(targetId))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<FollowListResponse> getFollowings(Long userId) {
        // 나(userId)가 follower 인 데이터 조회
        return followRepository.findAllByFollowerId(userId).stream()
                .map(follow -> {
                    Long targetId = follow.getFollowingId();
                    return FollowListResponse.builder()
                            .userId(targetId)
                            .nickname(getUserNickname(targetId))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String getUserNickname(Long userId) {
        try {
            // getUser() 호출 후 닉네임 추출
            UserClientResponse userDto = userClient.getUser(userId);
            if (userDto != null) {
                return userDto.getNickname();
            }
        } catch (Exception e) {
            log.warn("User Service Error: {}", e.getMessage());
        }
        return "Unknown";
    }
}
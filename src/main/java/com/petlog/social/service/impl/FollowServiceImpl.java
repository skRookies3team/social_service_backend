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
        if (followerId.equals(targetId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "자기 자신을 팔로우할 수 없습니다.");
        }

        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowingId(followerId, targetId);

        if (existingFollow.isPresent()) {
            followRepository.delete(existingFollow.get());
            return false;
        } else {
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
                    // [수정] 유저 정보 전체 조회
                    UserClientResponse userInfo = getUserInfo(targetId);

                    return FollowListResponse.builder()
                            .userId(targetId)
                            .nickname(userInfo != null ? userInfo.getUsername() : "Unknown")
                            .profileImageUrl(userInfo != null ? userInfo.getProfileImage() : null) // [추가] 이미지 매핑
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
                    // [수정] 유저 정보 전체 조회
                    UserClientResponse userInfo = getUserInfo(targetId);

                    return FollowListResponse.builder()
                            .userId(targetId)
                            .nickname(userInfo != null ? userInfo.getUsername() : "Unknown")
                            .profileImageUrl(userInfo != null ? userInfo.getProfileImage() : null) // [추가] 이미지 매핑
                            .build();
                })
                .collect(Collectors.toList());
    }

    // [수정] 메서드 변경: 닉네임 문자열 대신 유저 정보 객체(UserClientResponse) 반환
    private UserClientResponse getUserInfo(Long userId) {
        try {
            UserClientResponse userDto = userClient.getUser(userId);
            if (userDto != null) {
                return userDto;
            }
        } catch (Exception e) {
            log.warn("User Service Error: {}", e.getMessage());
        }
        return null;
    }
}
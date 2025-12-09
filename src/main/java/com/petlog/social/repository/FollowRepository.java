package com.petlog.social.repository;

import com.petlog.social.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    // 1. 팔로우 여부 확인 (토글용)
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // 2. 팔로잉 수 (내가 팔로우하는 사람 수)
    long countByFollowerId(Long followerId);

    // 3. 팔로워 수 (나를 팔로우하는 사람 수)
    long countByFollowingId(Long followingId);

    // 4. 내가 팔로우하는 사람 목록 (팔로잉 리스트)
    List<Follow> findAllByFollowerId(Long followerId);

    // 5. 나를 팔로우하는 사람 목록 (팔로워 리스트)
    List<Follow> findAllByFollowingId(Long followingId);
}
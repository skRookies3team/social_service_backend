package com.petlog.social.repository;

import com.petlog.social.entity.Feed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {
    // 1. 전체 피드 조회 (페이징 적용)
    Slice<Feed> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 2. 특정 유저의 피드 조회 (페이징 적용)
    Slice<Feed> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 3. 게시글 수 조회 (기존 유지)
    Long countByUserId(Long userId);

    // 4. 팔로우한 유저 피드 조회 (페이징 적용)
    @Query("SELECT f FROM Feed f " +
            "WHERE f.userId IN (" +
            "   SELECT fw.followingId FROM Follow fw WHERE fw.followerId = :followerId" +
            ") " +
            "ORDER BY f.createdAt DESC")
    Slice<Feed> findAllByFollowingUsers(@Param("followerId") Long followerId, Pageable pageable);
}
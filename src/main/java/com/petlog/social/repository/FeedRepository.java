package com.petlog.social.repository;

import com.petlog.social.entity.Feed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    // 1. 전체 피드 조회 (기본)
    Slice<Feed> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // [추가] 1-1. 전체 피드 조회 (차단 필터링)
    Slice<Feed> findAllByUserIdNotInOrderByCreatedAtDesc(List<Long> userIds, Pageable pageable);

    // 2. 유저별 피드 조회
    Slice<Feed> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 3. 게시글 수 카운트
    Long countByUserId(Long userId);

    // 4. 팔로우한 유저 피드 조회 (뉴스피드)
    @Query("SELECT f FROM Feed f " +
            "WHERE f.userId IN (" +
            "   SELECT fw.followingId FROM Follow fw WHERE fw.followerId = :followerId" +
            ") " +
            "ORDER BY f.createdAt DESC")
    Slice<Feed> findAllByFollowingUsers(@Param("followerId") Long followerId, Pageable pageable);

    // 5. 인기 게시물 (기본)
    @Query("SELECT f FROM Feed f LEFT JOIN FeedLike fl ON f.id = fl.feed.id " +
            "WHERE f.createdAt >= :startDate " +
            "GROUP BY f.id " +
            "ORDER BY COUNT(fl.id) DESC, f.createdAt DESC")
    Slice<Feed> findTrendingFeeds(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    // [추가] 5-1. 인기 게시물 (차단 필터링)
    @Query("SELECT f FROM Feed f LEFT JOIN FeedLike fl ON f.id = fl.feed.id " +
            "WHERE f.createdAt >= :startDate " +
            "AND f.userId NOT IN :blockedIds " +  // 차단 유저 제외 조건 추가
            "GROUP BY f.id " +
            "ORDER BY COUNT(fl.id) DESC, f.createdAt DESC")
    Slice<Feed> findTrendingFeedsBlocked(@Param("startDate") LocalDateTime startDate, @Param("blockedIds") List<Long> blockedIds, Pageable pageable);

    // 6. 해시태그 검색 (기본)
    @Query("SELECT f FROM Feed f JOIN f.feedHashtags fh JOIN fh.hashtag h " +
            "WHERE h.name = :hashtag " +
            "ORDER BY f.createdAt DESC")
    Slice<Feed> findByHashtag(@Param("hashtag") String hashtag, Pageable pageable);

    // [추가] 6-1. 해시태그 검색 (차단 필터링)
    @Query("SELECT f FROM Feed f JOIN f.feedHashtags fh JOIN fh.hashtag h " +
            "WHERE h.name = :hashtag " +
            "AND f.userId NOT IN :blockedIds " + // 차단 유저 제외 조건 추가
            "ORDER BY f.createdAt DESC")
    Slice<Feed> findByHashtagBlocked(@Param("hashtag") String hashtag, @Param("blockedIds") List<Long> blockedIds, Pageable pageable);
}
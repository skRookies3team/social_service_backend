package com.petlog.social.repository;

import com.petlog.social.entity.Feed;
import com.petlog.social.entity.FeedLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {
    // 1. 토글용: 사용자가 특정 피드에 좋아요를 눌렀는지 객체로 조회
    Optional<FeedLike> findByFeedAndUserId(Feed feed, Long userId);

    // 2. 조회용: 사용자가 좋아요를 눌렀는지 여부만 빠르게 확인 (exists)
    boolean existsByFeedAndUserId(Feed feed, Long userId);

    // 3. 개수용: 피드의 총 좋아요 개수
    long countByFeed(Feed feed);

    // 4. 해당 피드의 모든 좋아요 리스트 조회
    List<FeedLike> findAllByFeed(Feed feed);
}
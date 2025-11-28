package com.example.petlog.repository;

import com.example.petlog.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {
    // 생성일 기준 내림차순(최신순)으로 모든 피드를 조회합니다.
    List<Feed> findAllByOrderByCreatedAtDesc();
}
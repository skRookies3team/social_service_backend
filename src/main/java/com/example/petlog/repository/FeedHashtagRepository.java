package com.example.petlog.repository;

import com.example.petlog.entity.FeedHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedHashtagRepository extends JpaRepository<FeedHashtag, Long> {
}
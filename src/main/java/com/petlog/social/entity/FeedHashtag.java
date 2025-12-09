package com.petlog.social.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [피드-해시태그 매핑 엔티티]
 * - 역할: Feed(게시물)와 Hashtag(태그) 사이의 다대다 관계를 풀어주는 중간 다리 역할입니다.
 * - 기능: "어떤 피드에 어떤 태그가 달렸는지"를 기록합니다.
 * - 예시:
 * - id=1, feed_id=100(게시물), hashtag_id=1(강아지)
 * - id=2, feed_id=100(게시물), hashtag_id=2(산책)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feed_hashtags")
public class FeedHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 피드인지 (Feed 테이블 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id")
    private Feed feed;

    // 어떤 태그인지 (Hashtag 테이블 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id")
    private Hashtag hashtag;

    @Builder
    public FeedHashtag(Feed feed, Hashtag hashtag) {
        this.feed = feed;
        this.hashtag = hashtag;
    }
}
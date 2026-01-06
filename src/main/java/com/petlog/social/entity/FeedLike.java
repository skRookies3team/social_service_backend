package com.petlog.social.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
// [핵심] 한 유저(user_id)가 한 피드(feed_id)에 좋아요를 중복해서 넣을 수 없도록 제약 조건 설정
@Table(name = "feed_likes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_feed_like", columnNames = {"feed_id", "user_id"})
})
public class FeedLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @CreatedDate
    private LocalDateTime createdAt;
}
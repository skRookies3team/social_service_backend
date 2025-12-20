package com.petlog.social.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "feeds")
public class Feed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // [삭제] private String imageUrl;

    // [추가] 이미지 여러 장 (1:N)
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedImage> feedImages = new ArrayList<>();

    @Column
    private String location;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "pet_id")
    private Long petId;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedHashtag> feedHashtags = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    // [수정] 생성자에서 imageUrl 제거
    public Feed(String content, String location, Long userId, Long petId) {
        this.content = content;
        this.location = location;
        this.userId = userId;
        this.petId = petId;
    }

    // [추가] 이미지 추가 메서드 (연관관계 편의 메서드)
    public void addImage(String url) {
        FeedImage image = FeedImage.builder()
                .imageUrl(url)
                .feed(this)
                .build();
        this.feedImages.add(image);
    }

    // [추가] 이미지 목록 전체 교체 (수정 시 사용)
    public void updateImages(List<String> newUrls) {
        this.feedImages.clear(); // 기존 이미지 삭제 (orphanRemoval = true로 인해 DB에서도 삭제됨)
        if (newUrls != null) {
            for (String url : newUrls) {
                addImage(url);
            }
        }
    }

    // [수정] 내용 및 위치 수정
    public void updateContent(String content, String location) {
        this.content = content;
        this.location = location;
    }
}
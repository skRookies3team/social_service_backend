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

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedImage> feedImages = new ArrayList<>();

    @Column
    private String location;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "pet_id")
    private Long petId;

    // [수정] columnDefinition 추가 -> 기존 데이터에 'PUBLIC' 문자열을 기본값으로 채워넣음
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'PUBLIC'")
    private Visibility visibility = Visibility.PUBLIC;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedHashtag> feedHashtags = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Feed(String content, String location, Long userId, Long petId, Visibility visibility) {
        this.content = content;
        this.location = location;
        this.userId = userId;
        this.petId = petId;
        if (visibility != null) {
            this.visibility = visibility;
        }
    }

    // --- 연관관계 및 편의 메서드 ---

    public void addImage(String url) {
        FeedImage image = FeedImage.builder()
                .imageUrl(url)
                .feed(this)
                .build();
        this.feedImages.add(image);
    }

    public void updateImages(List<String> newUrls) {
        this.feedImages.clear();
        if (newUrls != null) {
            for (String url : newUrls) {
                addImage(url);
            }
        }
    }

    public void updateContent(String content, String location) {
        this.content = content;
        this.location = location;
    }

    public void updateVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
}
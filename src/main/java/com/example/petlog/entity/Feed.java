package com.example.petlog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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

    @Column(name = "image_url")
    private String imageUrl;

    @Column
    private String location;

    // MSA 전환: 객체 참조(User) 대신 ID(Long) 저장
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // MSA 전환: 객체 참조(Pet) 대신 ID(Long) 저장
    @Column(name = "pet_id")
    private Long petId;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Feed(String content, String imageUrl, String location, Long userId, Long petId) {
        this.content = content;
        this.imageUrl = imageUrl;
        this.location = location;
        this.userId = userId;
        this.petId = petId;
    }

    public void updateFeed(String content, String imageUrl, String location) {
        this.content = content;
        this.imageUrl = imageUrl;
        this.location = location;
    }
}

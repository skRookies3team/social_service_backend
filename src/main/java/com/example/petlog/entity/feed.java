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

// 피드 테이블과 매핑되는 엔티티 클래스
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성을 막기 위해 protected 사용
@EntityListeners(AuditingEntityListener.class) // 생성일, 수정일 자동 관리를 위한 리스너
@Table(name = "feeds")
public class Feed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 피드 내용은 텍스트 길이가 길 수 있으므로 TEXT 타입으로 정의
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    // 작성자 정보 (User 엔티티와 다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 관련 반려동물 정보 (Pet 엔티티와 다대일 관계, 선택사항이므로 null 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Feed(String content, String imageUrl, User user, Pet pet) {
        this.content = content;
        this.imageUrl = imageUrl;
        this.user = user;
        this.pet = pet;
    }

    // 피드 수정 메서드 (내용과 이미지만 변경 가능)
    public void updateFeed(String content, String imageUrl) {
        this.content = content;
        this.imageUrl = imageUrl;
    }
}
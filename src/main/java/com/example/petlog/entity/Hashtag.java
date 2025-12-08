package com.example.petlog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [해시태그 마스터 엔티티]
 * - 역할: 서비스 전체에서 사용되는 태그 이름(단어)을 중복 없이 관리합니다.
 * - 예시: id=1, name="강아지" / id=2, name="산책"
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "hashtags")
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 태그 이름 (중복될 수 없도록 unique = true 설정)
    // 100개의 피드에 #강아지 가 달려도, 여기에는 '강아지' 데이터가 1개만 저장됩니다.
    @Column(nullable = false, unique = true)
    private String name;

    public Hashtag(String name) {
        this.name = name;
    }
}
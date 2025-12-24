package com.petlog.social.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reporterId; // 신고자 ID

    @Column(nullable = false)
    private Long targetId; // 신고 대상 ID (피드ID, 유저ID, 댓글ID 등)

    @Column(nullable = false)
    private String type; // FEED, USER, COMMENT

    @Column(columnDefinition = "TEXT")
    private String reason; // 신고 사유

    @CreatedDate
    private LocalDateTime createdAt;
}
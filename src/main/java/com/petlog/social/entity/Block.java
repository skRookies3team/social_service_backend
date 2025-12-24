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
@Table(name = "blocks",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"blockerId", "blockedId"})
        })
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long blockerId; // 차단한 사람 (나)

    @Column(nullable = false)
    private Long blockedId; // 차단당한 사람 (상대방)

    @CreatedDate
    private LocalDateTime createdAt;
}
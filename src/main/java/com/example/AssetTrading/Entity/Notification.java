package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_idx")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx")
    private User user; // 알림 수신자

    @Column(name = "title", nullable = false)
    private String title; // 알림 제목

    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // 알림 내용

    @Column(name = "notification_type")
    private String notificationType; // 알림 유형 (TRANSACTION, CHAT, SYSTEM 등)

    @Column(name = "reference_id")
    private Long referenceId; // 참조 ID (거래 ID, 채팅 ID 등)
    
    @Column(name = "is_read", columnDefinition = "boolean default false")
    private Boolean isRead; // 읽음 여부
    
    @Column(name = "created_at")
    private LocalDateTime createdAt; // 생성 시간
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }
} 
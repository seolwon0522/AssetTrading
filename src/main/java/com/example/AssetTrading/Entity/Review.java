package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_idx")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_idx")
    private Transaction transaction; // 거래 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_idx")
    private User reviewer; // 리뷰 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_idx")
    private User reviewed; // 리뷰 대상

    @Column(name = "rating", nullable = false)
    private Integer rating; // 평점 (1-5)

    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // 리뷰 내용

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 작성 시간

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정 시간
    
    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private Boolean isDeleted; // 삭제 여부
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isDeleted == null) {
            isDeleted = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 
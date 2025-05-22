package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_idx")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_idx")
    private Transaction transaction; // 거래 정보

    @Column(name = "payment_amount", nullable = false)
    private Integer amount; // 결제 금액

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // 결제 방법 (CARD, BANK_TRANSFER, VIRTUAL_ACCOUNT 등)

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus; // 결제 상태 (PENDING, COMPLETED, FAILED, REFUNDED 등)

    @Column(name = "payment_key")
    private String paymentKey; // 외부 결제 시스템 키

    @Column(name = "payment_approved_at")
    private LocalDateTime approvedAt; // 결제 승인 시간

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 생성 시간

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정 시간

    @Column(name = "refund_reason")
    private String refundReason; // 환불 사유
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt; // 환불 시간
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 
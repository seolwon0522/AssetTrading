package com.example.AssetTrading.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    Long id;
    Long productId;
    Long buyerId;
    Long sellerId;

    //satatus enum 이게맞나
    enum TransactionStatus {
        Approved,
        Requested,
        Completed,
        Canceled
    }
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

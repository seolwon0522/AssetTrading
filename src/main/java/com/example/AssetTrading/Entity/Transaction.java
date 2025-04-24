package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transaction_idx;

    private int product_idx;

    private String buyer_user_id;

    private String seller_user_id;

    @Enumerated(EnumType.STRING)
    private TransactionState transaction_state;

    private LocalDateTime completed_date;

    public enum TransactionState {
        Requested,
        Approved,
        Completed,
        Canceled
    }
}

package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.Transaction;
import com.example.AssetTrading.Entity.TransactionStatus;
import com.example.AssetTrading.Entity.User;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TransactionRequestDto {
//    private Long buyerId;
//    private String sellerId;
//    private String sellProductId;
//    private TransactionStatus status;
//
//    @Builder
//    public Transaction toEntity() {
//        return Transaction.builder()
//                .buyer_idx(buyerId)
//                .seller_idx(sellerId)
//                .
//        this.sellerId = sellerId;
//        this.sellProductId = sellProductId;
//        this.status = status;
//    }
}

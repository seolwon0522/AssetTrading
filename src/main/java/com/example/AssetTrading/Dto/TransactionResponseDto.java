package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.Transaction;
import com.example.AssetTrading.Entity.TransactionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDto {

    private Long transaction_idx;      // 거래 ID
    private Long product_idx;          // 상품 ID
    private String buyer_user_idx;     // 구매자 ID
    private String seller_user_idx;    // 판매자 ID
    private TransactionStatus status;  // 거래 상태 (enum)
    private LocalDateTime created_at;  // 거래 요청 시간

        public static TransactionResponseDto fromEntity(Transaction transaction) {
            return TransactionResponseDto.builder()
                    .transaction_idx(transaction.getTransaction_idx())
                    .buyer_user_idx(transaction.getBuyer().getUserId())
                    .seller_user_idx(transaction.getSeller().getUserId())
                    .product_idx(transaction.getSellProduct().getId())
                    .status(transaction.getStatus())
                    .created_at(LocalDateTime.now())
                    .build();
        }
    }
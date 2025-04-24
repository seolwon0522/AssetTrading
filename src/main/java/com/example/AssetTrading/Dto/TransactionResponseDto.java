package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.Transaction;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDto {

    private Long transaction_idx;             // 거래 ID
    private int product_idx;                  // 상품 ID
    private String buyer_user_id;             // 구매자 ID
    private String seller_user_id;            // 판매자 ID
    private Transaction.TransactionState transaction_state; // 거래 상태 (enum)
    private LocalDateTime completed_date;     // 거래 완료일

    public static TransactionResponseDto fromEntity(Transaction transaction) {
        return TransactionResponseDto.builder()
                .transaction_idx(transaction.getTransaction_idx())
                .product_idx(transaction.getProduct_idx())
                .buyer_user_id(transaction.getBuyer_user_id())
                .seller_user_id(transaction.getSeller_user_id())
                .transaction_state(transaction.getTransaction_state())
                .completed_date(transaction.getCompleted_date())
                .build();
    }
}

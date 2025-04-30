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

    private Long transaction_idx;             // 거래 ID
    private Long product_idx;                 // 상품 ID
    private String buyer_user_id;             // 구매자 ID
    private String seller_user_id;            // 판매자 ID
    private TransactionStatus status;         // 거래 상태 (enum)
    private LocalDateTime completed_date;     // 거래 완료일

    public static TransactionResponseDto fromEntity(Transaction transaction) {
        return TransactionResponseDto.builder()
                .transaction_idx(transaction.getTransaction_idx())
                .product_idx(transaction.getSellProduct().getId())
                .buyer_user_id(String.valueOf(transaction.getBuyer().getUser_idx()))
                .seller_user_id(String.valueOf(transaction.getSeller().getUser_idx()))
                .status(transaction.getStatus())
                .completed_date(transaction.getFinishTime())
                .build();
    }
}

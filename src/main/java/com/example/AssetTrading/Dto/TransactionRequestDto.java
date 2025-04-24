package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.Transaction;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequestDto {
    private int product_idx;           // 상품 ID
    private String buyer_user_id;     // 구매자 ID

    public Transaction toEntity(String seller_user_id) {
        return Transaction.builder()
                .product_idx(this.product_idx)
                .buyer_user_id(this.buyer_user_id)
                .seller_user_id(seller_user_id)
                .transaction_state(Transaction.TransactionState.Requested)
                .completed_date(LocalDateTime.now())
                .build();
    }
}
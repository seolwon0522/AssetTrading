package com.example.AssetTrading.Dto;

<<<<<<< Updated upstream
import com.example.AssetTrading.Entity.Transaction;
import lombok.*;

import java.time.LocalDateTime;

import lombok.*;

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
                .buyer(this.buyer_user_id)
                .seller_user_id(seller_user_id)
                .transaction_state(Transaction.Transaction.Requested)
                .completed_date(LocalDateTime.now())
                .build();
    }
}
=======

public class TransactionRequestDto {
    private Long id;
    private Long productId;
    private Long buyerId;
    private Long sellerId;



}
>>>>>>> Stashed changes

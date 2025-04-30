package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.Transaction;
import com.example.AssetTrading.Entity.TransactionStatus;
import com.example.AssetTrading.Entity.User;
import com.example.AssetTrading.Entity.SellProduct;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TransactionRequestDto {
    private Long buyerId;
    private String sellerId;
    private String sellProductId;

    public Transaction toEntity(User buyer, User seller, SellProduct sellProduct) {
        return Transaction.builder()
                .buyer(buyer)
                .seller(seller)
                .sellProduct(sellProduct)
                .status(TransactionStatus.WAITTING)
                .build();
    }
}

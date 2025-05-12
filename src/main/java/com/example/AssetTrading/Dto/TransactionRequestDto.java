package com.example.AssetTrading.Dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// buyer, seller, product는 서비스에서 조회해야 하므로 외부 주입 필요
public class TransactionRequestDto {
    private Long buyerId;
    private Long productId;

}

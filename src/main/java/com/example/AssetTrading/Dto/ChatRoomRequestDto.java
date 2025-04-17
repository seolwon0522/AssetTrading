package com.example.AssetTrading.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomRequestDto {
    private Integer transaction_idx;     // 거래 ID
    private String buyer_user_id;        // 구매자 ID
    private String seller_user_id;       // 판매자 ID
}

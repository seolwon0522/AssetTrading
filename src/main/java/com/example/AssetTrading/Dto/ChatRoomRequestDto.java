package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.ChatRoom;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomRequestDto {
    private Integer transaction_idx;     // 거래 ID
    private Long buyer_user_idx;         // 구매자 ID (숫자)
    private String buyer_user_id;        // 구매자 ID (문자열)
    private String buyerName;            // 구매자 이름
    private Long seller_user_idx;        // 판매자 ID (숫자)
    private String seller_user_id;       // 판매자 ID (문자열)
    private String sellerName;           // 판매자 이름
    private Long product_idx;            // 상품 ID
    private String productTitle;         // 상품 제목
    
    public ChatRoom toEntity() {
        return ChatRoom.builder()
                .transactionIdx(transaction_idx)
                .buyerUserIdx(buyer_user_idx)
                .buyerUserId(buyer_user_id)
                .buyerName(buyerName)
                .sellerUserIdx(seller_user_idx)
                .sellerUserId(seller_user_id)
                .sellerName(sellerName)
                .productIdx(product_idx)
                .productTitle(productTitle)
                .build();
    }
}

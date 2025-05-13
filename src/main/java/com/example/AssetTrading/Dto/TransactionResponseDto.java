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
    private String productTitle;       // 상품 제목
    private Integer productPrice;      // 상품 가격
    private String productImg;         // 상품 이미지
    private Long buyer_user_idx;       // 구매자 ID (숫자)
    private String buyerId;            // 구매자 ID (문자열)
    private String buyerName;          // 구매자 이름
    private Long seller_user_idx;      // 판매자 ID (숫자)
    private String sellerId;           // 판매자 ID (문자열)
    private String sellerName;         // 판매자 이름
    private TransactionStatus status;  // 거래 상태 (enum)
    private LocalDateTime created_at;  // 거래 요청 시간
    private LocalDateTime processed_at; // 거래 승인 시간
    private LocalDateTime success_at;  // 거래 완료 시간
    private LocalDateTime deleted_at;  // 거래 취소 시간

    public static TransactionResponseDto fromEntity(Transaction transaction) {
        return TransactionResponseDto.builder()
                .transaction_idx(transaction.getTransactionIdx())
                .product_idx(transaction.getSellProduct().getId())
                .productTitle(transaction.getSellProduct().getProductTitle())
                .productPrice(transaction.getSellProduct().getProductPrice())
                .productImg(transaction.getSellProduct().getProductImg())
                .buyer_user_idx(transaction.getBuyer().getUser_idx())
                .buyerId(transaction.getBuyer().getUserId())
                .buyerName(transaction.getBuyer().getUserName())
                .seller_user_idx(transaction.getSeller().getUser_idx())
                .sellerId(transaction.getSeller().getUserId())
                .sellerName(transaction.getSeller().getUserName())
                .status(transaction.getStatus())
                .created_at(transaction.getCreatedTime())
                .processed_at(transaction.getProcessedTime())
                .success_at(transaction.getSuccessTime())
                .deleted_at(transaction.getDeletedTime())
                .build();
    }
}
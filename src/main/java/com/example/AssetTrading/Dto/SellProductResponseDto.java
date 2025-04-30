package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.SellProduct;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellProductResponseDto {
    private String productTitle;
    private String productDesc;
    private String productImg;
    private Integer productPrice;
    private Integer productQuantity;
    private String productCreateDate;
    private LocalDate productAvailDate;
    private Long sellerUserIdx;
    private String productStatus; // 여기는 String으로 반환

    // Entity → DTO 변환
    public static SellProductResponseDto fromEntity(SellProduct product) {
        return SellProductResponseDto.builder()
                .productTitle(product.getProductTitle())
                .productDesc(product.getProductDesc())
                .productImg(product.getProductImg())
                .productPrice(product.getProductPrice())
                .productQuantity(product.getProductQuantity())
                .productCreateDate(product.getProductCreateDate())
                .productAvailDate(product.getProductAvailDate())
                .sellerUserIdx(product.getSellerUserIdx())
                .productStatus(product.getProductStatus().name()) // enum을 문자열로 변환
                .build();
    }
}

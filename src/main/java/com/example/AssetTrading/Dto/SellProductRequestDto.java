package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.SellProduct;
import com.example.AssetTrading.Entity.ProductStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellProductRequestDto {
    private String productTitle;
    private String productDesc;
    private String productImg;
    private Integer productPrice;
    private Integer productQuantity;
    private String productCreateDate;
    private LocalDate productAvailDate;
    private String sellerUserId;
    private String productStatus; // 여기는 그대로 String

    public SellProduct toEntity() {
        return SellProduct.builder()
                .productTitle(this.productTitle)
                .productDesc(this.productDesc)
                .productImg(this.productImg)
                .productPrice(this.productPrice)
                .productQuantity(this.productQuantity)
                .productCreateDate(this.productCreateDate)
                .productAvailDate(this.productAvailDate)
                .sellerUserId(this.sellerUserId)
                .productStatus(ProductStatus.valueOf(this.productStatus)) // 이 부분에서 변환
                .build();
    }
}

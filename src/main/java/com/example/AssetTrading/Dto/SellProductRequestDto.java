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
    private Long sellerUserIdx;
    private String productStatus; // 여기는 그대로 String
    private String productCategory;
    private String productTags;
    private Boolean featured;

    public SellProduct toEntity() {
        return SellProduct.builder()
                .productTitle(this.productTitle)
                .productDesc(this.productDesc)
                .productImg(this.productImg)
                .productPrice(this.productPrice)
                .productQuantity(this.productQuantity)
                .productCreateDate(this.productCreateDate)
                .productAvailDate(this.productAvailDate)
                .sellerUserIdx(this.sellerUserIdx)
                .productStatus(ProductStatus.valueOf(this.productStatus))
                .productCategory(this.productCategory)
                .productTags(this.productTags)
                .viewCount(0)
                .featured(this.featured != null ? this.featured : false)
                .build();
    }
}

package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_idx")
    private Long id; // 상품 인덱스

    @Column(name = "product_title", nullable = false)
    private String productTitle; // 상품 제목

    @Column(name = "product_desc", columnDefinition = "TEXT")
    private String productDesc; // 상품 설명

    @Column(name = "product_image")
    private String productImg; // 상품 이미지

    @Column(name = "product_price", nullable = false)
    private Integer productPrice; // 상품 가격

    @Column(name = "product_quantity", nullable = false)
    private Integer productQuantity; // 상품 수량

    @Column(name = "product_create_date")
    private String productCreateDate; // 상품 등록일

    @Column(name = "product_avail_date")
    private LocalDate productAvailDate; // 상품 판매 가능일

    @Column(name = "seller_user_idx", nullable = false)
    private Long sellerUserIdx; // 판매자 인덱스

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status")
    private ProductStatus productStatus; // 상품 상태
    
    @Column(name = "product_category")
    private String productCategory; // 상품 카테고리
    
    @Column(name = "product_tags")
    private String productTags; // 상품 태그
    
    @Column(name = "view_count", columnDefinition = "integer default 0")
    private Integer viewCount; // 조회수
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated; // 마지막 업데이트 시간
    
    @Column(name = "featured", columnDefinition = "boolean default false")
    private Boolean featured; // 추천 상품 여부
    
    @PrePersist
    protected void onCreate() {
        lastUpdated = LocalDateTime.now();
        if (viewCount == null) {
            viewCount = 0;
        }
        if (featured == null) {
            featured = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}

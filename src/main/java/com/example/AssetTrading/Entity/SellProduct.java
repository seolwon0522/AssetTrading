package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

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
    private Long id;

    @Column(name = "product_title", columnDefinition = "TEXT", nullable = false)
    private String productTitle;

    @Column(name = "product_desc", length = 255)
    private String productDesc;

    @Column(name = "product_img", columnDefinition = "TEXT")
    private String productImg;

    @Column(name = "product_price", nullable = false)
    private Integer productPrice;

    @Column(name = "product_quantity", nullable = false)
    private Integer productQuantity;

    @Column(name = "product_create_date", length = 50)
    private String productCreateDate;

    @Column(name = "product_avail_date")
    private LocalDate productAvailDate;

    @Column(name = "seller_user_id", length = 100)
    private String sellerUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", length = 30)
    private ProductStatus productStatus;
}

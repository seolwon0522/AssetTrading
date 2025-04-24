package com.example.AssetTrading.Entity;

import com.example.AssetTrading.Entity.ProductStatus;
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
    @Column(name = "product_idx")
    private Long id;

    @Column(name = "product_title")
    private String productTitle;

    @Column(name = "product_desc")
    private String productDesc;

    @Column(name = "product_image")
    private String productImg;

    @Column(name = "product_price")
    private Integer productPrice;

    @Column(name = "product_quantity")
    private Integer productQuantity;

    @Column(name = "product_create_date")
    private String productCreateDate;

    @Column(name = "product_avail_date")
    private LocalDate productAvailDate;

    @Column(name = "seller_user_id")
    private String sellerUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status")
    private ProductStatus productStatus;



}

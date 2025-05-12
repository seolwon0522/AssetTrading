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

    // 외래키로 연관 잡아야함
    // 판매자
    @ManyToOne
    @JoinColumn(name = "seller_user_idx")
    private User sellerUserIdx; // 이름 seller로 바꿔도 됨. transaction에서는 seller로 하긴했음.

    ///  기존 seller 코드임 인지했으면 지워도 됨.
//    @Column(name = "seller_user_idx")
//    private Long sellerUserIdx;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status")
    private ProductStatus productStatus;



}

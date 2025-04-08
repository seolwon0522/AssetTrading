package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
@Getter
@Setter
@Entity
@Table(name="sellProducts")
public class SellProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String title;
    String description;
    String imageUrl;
    Integer price;
    Integer quantity;
    Date availableDate;
    Boolean shippingAvailable;
    enum SellProductStatus {
        Approved,
        Requested,
        Completed,
        Canceled
    }
    Long sellerId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

}

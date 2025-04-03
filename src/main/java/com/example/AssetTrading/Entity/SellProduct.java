package com.example.AssetTrading.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name="SellProduct")
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

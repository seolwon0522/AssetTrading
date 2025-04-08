package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "chatroom")

public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Long transactionId;
    Long buyerId;
    Long sellerId;
    LocalDateTime createdAt;


}

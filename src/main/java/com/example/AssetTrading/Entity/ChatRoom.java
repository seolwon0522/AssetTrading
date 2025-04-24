package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "chat_room") // 실제 DB 테이블명
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_idx")
    private Integer chat_room_idx;

    @Column(name = "transaction_idx")
    private Integer transaction_idx;

    @Column(name = "buyer_user_id", length = 40)
    private String buyer_user_id;

    @Column(name = "seller_user_id", length = 40)
    private String seller_user_id;

    @Column(name = "chat_room_create")
    private LocalDateTime chat_room_create;
}

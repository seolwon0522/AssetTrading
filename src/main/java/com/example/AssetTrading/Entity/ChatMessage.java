package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "chat_msg")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_msg_idx")
    private Integer chat_msg_idx;

    @Column(name = "chat_room_idx")
    private Integer chat_room_idx;

    @Column(name = "sender_user_idx")
    private Integer sender_user_idx;

    @Column(name = "chat_msg_content")
    private String chat_msg_content;

    @Column(name = "chat_send_date")
    private LocalDateTime chat_send_date;
}

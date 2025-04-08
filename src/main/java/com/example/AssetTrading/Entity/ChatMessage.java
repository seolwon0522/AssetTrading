package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="chatmessages")

public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Long roomId;
    Long senderId;
    String content;
    LocalDateTime sentAt;

}

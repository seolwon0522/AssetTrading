package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_room") // 실제 DB 테이블명
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_idx")
    private Integer chatRoomIdx; // 채팅방 인덱스

    @Column(name = "transaction_idx")
    private Integer transactionIdx; // 거래 인덱스

    @Column(name = "buyer_user_id", length = 40)
    private String buyerUserId; // 구매자 ID
    
    @Column(name = "buyer_user_idx")
    private Long buyerUserIdx; // 구매자 인덱스
    
    @Column(name = "buyer_name")
    private String buyerName; // 구매자 이름

    @Column(name = "seller_user_id", length = 40)
    private String sellerUserId; // 판매자 ID
    
    @Column(name = "seller_user_idx")
    private Long sellerUserIdx; // 판매자 인덱스
    
    @Column(name = "seller_name")
    private String sellerName; // 판매자 이름
    
    @Column(name = "product_idx")
    private Long productIdx; // 상품 인덱스
    
    @Column(name = "product_title")
    private String productTitle; // 상품 제목

    @Column(name = "chat_room_create")
    private LocalDateTime chatRoomCreate; // 채팅방 생성 시간
    
    @Column(name = "last_message")
    private String lastMessage; // 마지막 메시지
    
    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime; // 마지막 메시지 시간
    
    @Column(name = "unread_count")
    private Integer unreadCount; // 읽지 않은 메시지 수
    
    @PrePersist
    protected void onCreate() {
        if (chatRoomCreate == null) {
            chatRoomCreate = LocalDateTime.now();
        }
        if (unreadCount == null) {
            unreadCount = 0;
        }
    }
}

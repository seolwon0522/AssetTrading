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
@Table(name = "chat_msg")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_msg_idx")
    private Integer chatMsgIdx; // 채팅 메시지 인덱스

    @Column(name = "chat_room_idx")
    private Integer chatRoomIdx; // 채팅방 인덱스

    @Column(name = "sender_user_idx")
    private Integer senderUserIdx; // 발신자 사용자 인덱스
    
    @Column(name = "sender_name")
    private String senderName; // 발신자 이름
    
    @Column(name = "is_read", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean isRead; // 읽음 여부

    @Column(name = "chat_msg_content", columnDefinition = "TEXT")
    private String chatMsgContent; // 채팅 메시지 내용

    @Column(name = "chat_send_date")
    private LocalDateTime chatSendDate; // 채팅 전송 시간
    
    // 웹소켓 통신을 위한 메시지 타입 - 데이터베이스에 저장되지 않음
    public enum MessageType {
        CHAT,       // 일반 채팅 메시지
        JOIN,       // 사용자 입장
        LEAVE,      // 사용자 퇴장
        TRANSACTION_UPDATE  // 거래 상태 업데이트
    }
    
    @Transient // 이 필드는 데이터베이스에 저장되지 않음
    private MessageType type;
    
    @PrePersist
    protected void onCreate() {
        if (chatSendDate == null) {
            chatSendDate = LocalDateTime.now();
        }
    }
}

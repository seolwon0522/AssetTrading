package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.ChatMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequestDto {

    private Integer chatRoomIdx;       // 채팅방 ID
    private Integer senderUserIdx;     // 보낸 사람 ID
    private String senderName;           // 보낸 사람 이름
    private String chatMsgContent;     // 메시지 내용
    private ChatMessage.MessageType type; // 메시지 타입
    
    public ChatMessage toEntity() {
        return ChatMessage.builder()
                .chatRoomIdx(chatRoomIdx)
                .senderUserIdx(senderUserIdx)
                .senderName(senderName)
                .chatMsgContent(chatMsgContent)
                .isRead(false)
                .type(type)
                .build();
    }
}

package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.ChatMessage;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponseDto {

    private Integer chatMsgIdx;         // 메시지 ID
    private Integer chatRoomIdx;        // 채팅방 ID
    private Integer senderUserIdx;      // 보내는 사람 ID
    private String senderName;            // 보내는 사람 이름
    private String chatMsgContent;      // 메시지 내용
    private LocalDateTime chatSendDate; // 보낸 날짜
    private boolean isRead;               // 읽음 여부
    private ChatMessage.MessageType type; // 메시지 타입

    public static ChatMessageResponseDto fromEntity(ChatMessage message) {
        return ChatMessageResponseDto.builder()
                .chatMsgIdx(message.getChatMsgIdx())
                .chatRoomIdx(message.getChatRoomIdx())
                .senderUserIdx(message.getSenderUserIdx())
                .senderName(message.getSenderName())
                .chatMsgContent(message.getChatMsgContent())
                .chatSendDate(message.getChatSendDate())
                .isRead(message.isRead())
                .type(message.getType())
                .build();
    }
}

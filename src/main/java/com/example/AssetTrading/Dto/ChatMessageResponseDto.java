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

    private Integer chat_msg_idx;         // 메시지 ID
    private Integer chat_room_idx;        // 채팅방 ID
    private Integer sender_user_idx;      // 보내는 사람 ID
    private String chat_msg_content;      // 메시지 내용
    private LocalDateTime chat_send_date; // 보낸 날짜

    public static ChatMessageResponseDto fromEntity(ChatMessage message) {
        return ChatMessageResponseDto.builder()
                .chat_msg_idx(message.getChat_msg_idx())
                .chat_room_idx(message.getChat_room_idx())
                .sender_user_idx(message.getSender_user_idx())
                .chat_msg_content(message.getChat_msg_content())
                .chat_send_date(message.getChat_send_date())
                .build();
    }
}

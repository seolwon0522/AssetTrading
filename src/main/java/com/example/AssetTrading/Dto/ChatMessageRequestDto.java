package com.example.AssetTrading.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequestDto {

    private Integer chat_room_idx;       // 채팅방 ID
    private Integer sender_user_idx;     // 보낸 사람 ID
    private String chat_msg_content;     // 메시지 내용
}

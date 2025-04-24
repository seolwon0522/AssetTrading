package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.ChatRoom;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponseDto {

    private Integer chat_room_idx;
    private Integer transaction_idx;
    private String buyer_user_id;
    private String seller_user_id;
    private LocalDateTime chat_room_create;

    public static ChatRoomResponseDto fromEntity(ChatRoom chatRoom) {
        return ChatRoomResponseDto.builder()
                .chat_room_idx(chatRoom.getChat_room_idx())
                .transaction_idx(chatRoom.getTransaction_idx())
                .buyer_user_id(chatRoom.getBuyer_user_id())
                .seller_user_id(chatRoom.getSeller_user_id())
                .chat_room_create(chatRoom.getChat_room_create())
                .build();
    }
}

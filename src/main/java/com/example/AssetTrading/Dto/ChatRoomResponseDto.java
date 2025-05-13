package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.ChatRoom;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponseDto {

    private Integer chatRoomIdx;
    private Integer transactionIdx;
    private Long buyerUserIdx;
    private String buyerUserId;
    private String buyerName;
    private Long sellerUserIdx;
    private String sellerUserId;
    private String sellerName;
    private Long productIdx;
    private String productTitle;
    private LocalDateTime chatRoomCreate;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;
    private List<ChatMessageResponseDto> messages;

    public static ChatRoomResponseDto fromEntity(ChatRoom chatRoom) {
        return ChatRoomResponseDto.builder()
                .chatRoomIdx(chatRoom.getChatRoomIdx())
                .transactionIdx(chatRoom.getTransactionIdx())
                .buyerUserIdx(chatRoom.getBuyerUserIdx())
                .buyerUserId(chatRoom.getBuyerUserId())
                .buyerName(chatRoom.getBuyerName())
                .sellerUserIdx(chatRoom.getSellerUserIdx())
                .sellerUserId(chatRoom.getSellerUserId())
                .sellerName(chatRoom.getSellerName())
                .productIdx(chatRoom.getProductIdx())
                .productTitle(chatRoom.getProductTitle())
                .chatRoomCreate(chatRoom.getChatRoomCreate())
                .lastMessage(chatRoom.getLastMessage())
                .lastMessageTime(chatRoom.getLastMessageTime())
                .unreadCount(chatRoom.getUnreadCount())
                .build();
    }
    
    // Add messages to the response
    public ChatRoomResponseDto withMessages(List<ChatMessageResponseDto> messages) {
        this.messages = messages;
        return this;
    }
}

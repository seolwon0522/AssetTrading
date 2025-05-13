package com.example.AssetTrading.Repository;

import com.example.AssetTrading.Entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    
    // Find messages by chat room ID
    List<ChatMessage> findByChatRoomIdxOrderByChatSendDateAsc(Integer chatRoomIdx);
    
    // Find messages by chat room ID with pagination
    Page<ChatMessage> findByChatRoomIdxOrderByChatSendDateDesc(Integer chatRoomIdx, Pageable pageable);
    
    // Count unread messages in a chat room
    long countByChatRoomIdxAndSenderUserIdxNot(Integer chatRoomIdx, Integer userId);
    
    // Delete all messages in a chat room
    void deleteByChatRoomIdx(Integer chatRoomIdx);
} 
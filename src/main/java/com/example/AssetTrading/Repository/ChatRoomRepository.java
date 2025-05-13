package com.example.AssetTrading.Repository;

import com.example.AssetTrading.Entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    
    // 거래id로 채팅룸찾기
    Optional<ChatRoom> findByTransactionIdx(Integer transactionIdx);
    
    // buyerid로 채팅룸찾기
    List<ChatRoom> findByBuyerUserId(String buyerUserId);
    
    // sellerid로 채팅룸찾기
    List<ChatRoom> findBySellerUserId(String sellerUserId);
    
    // buyerid 또는 sellerid로 채팅룸찾기
    List<ChatRoom> findByBuyerUserIdOrSellerUserId(String buyerUserId, String sellerUserId);
    
    // 거래id로 채팅룸존재여부확인
    boolean existsByTransactionIdx(Integer transactionIdx);
} 
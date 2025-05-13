package com.example.AssetTrading.Service;

import com.example.AssetTrading.Dto.ChatMessageRequestDto;
import com.example.AssetTrading.Dto.ChatMessageResponseDto;
import com.example.AssetTrading.Dto.ChatRoomRequestDto;
import com.example.AssetTrading.Dto.ChatRoomResponseDto;
import com.example.AssetTrading.Entity.ChatMessage;
import com.example.AssetTrading.Entity.ChatRoom;
import com.example.AssetTrading.Entity.Transaction;
import com.example.AssetTrading.Exception.ChatException;
import com.example.AssetTrading.Repository.ChatMessageRepository;
import com.example.AssetTrading.Repository.ChatRoomRepository;
import com.example.AssetTrading.Repository.TransactionRepository;
import com.example.AssetTrading.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // 새 채팅방 생성
    @Transactional
    public ChatRoomResponseDto createChatRoom(ChatRoomRequestDto requestDto) {
        // 이미 해당 거래에 대한 채팅방이 있는지 확인
        if (chatRoomRepository.existsByTransactionIdx(requestDto.getTransaction_idx())) {
            throw new ChatException("이미 해당 거래에 대한 채팅방이 존재합니다.", "CHAT_ROOM_EXISTS");
        }
        
        // 채팅방 생성 및 저장
        ChatRoom chatRoom = requestDto.toEntity();
        chatRoom.setChatRoomCreate(LocalDateTime.now());
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        
        // 채팅방 생성을 알리는 시스템 메시지 생성
        ChatMessage systemMessage = ChatMessage.builder()
                .chatRoomIdx(savedRoom.getChatRoomIdx())
                .senderUserIdx(0) // 시스템 메시지
                .senderName("System")
                .chatMsgContent("채팅방이 생성되었습니다.")
                .chatSendDate(LocalDateTime.now())
                .isRead(true)
                .type(ChatMessage.MessageType.JOIN)
                .build();
        
        chatMessageRepository.save(systemMessage);
        
        // 채팅방 초기 메시지로 업데이트
        savedRoom.setLastMessage(systemMessage.getChatMsgContent());
        savedRoom.setLastMessageTime(systemMessage.getChatSendDate());
        chatRoomRepository.save(savedRoom);
        
        return ChatRoomResponseDto.fromEntity(savedRoom);
    }
    
    // ID로 채팅방 조회
    public ChatRoomResponseDto getChatRoom(Integer roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException("채팅방을 찾을 수 없습니다.", "CHAT_ROOM_NOT_FOUND"));
        
        // 채팅방의 최신 메시지 가져오기
        List<ChatMessageResponseDto> messages = chatMessageRepository
                .findByChatRoomIdxOrderByChatSendDateAsc(roomId)
                .stream()
                .map(ChatMessageResponseDto::fromEntity)
                .collect(Collectors.toList());
        
        return ChatRoomResponseDto.fromEntity(chatRoom).withMessages(messages);
    }
    
    // 사용자별 채팅방 조회
    public List<ChatRoomResponseDto> getChatRoomsByUser(String userId) {
        return chatRoomRepository.findByBuyerUserIdOrSellerUserId(userId, userId)
                .stream()
                .map(ChatRoomResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 거래 ID로 채팅방 조회
    public ChatRoomResponseDto getChatRoomByTransaction(Integer transactionId) {
        ChatRoom chatRoom = chatRoomRepository.findByTransactionIdx(transactionId)
                .orElseThrow(() -> new ChatException("해당 거래에 대한 채팅방이 없습니다.", "CHAT_ROOM_NOT_FOUND"));
        
        return ChatRoomResponseDto.fromEntity(chatRoom);
    }
    
    // 메시지 전송
    @Transactional
    public ChatMessageResponseDto sendMessage(ChatMessageRequestDto requestDto) {
        // 채팅방 존재 여부 확인
        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getChatRoomIdx())
                .orElseThrow(() -> new ChatException("채팅방을 찾을 수 없습니다.", "CHAT_ROOM_NOT_FOUND"));
        
        // 메시지 생성 및 저장
        ChatMessage message = requestDto.toEntity();
        message.setChatSendDate(LocalDateTime.now());
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        // 채팅방 최신 메시지로 업데이트
        chatRoom.setLastMessage(message.getChatMsgContent());
        chatRoom.setLastMessageTime(message.getChatSendDate());
        chatRoom.setUnreadCount(chatRoom.getUnreadCount() + 1);
        chatRoomRepository.save(chatRoom);
        
        // 응답 DTO로 변환
        return ChatMessageResponseDto.fromEntity(savedMessage);
    }
    
    // 채팅방의 메시지 페이징 조회
    public Page<ChatMessageResponseDto> getMessages(Integer roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("chatSendDate").descending());
        
        return chatMessageRepository.findByChatRoomIdxOrderByChatSendDateDesc(roomId, pageable)
                .map(ChatMessageResponseDto::fromEntity);
    }
    
    // 메시지를 읽음으로 표시
    @Transactional
    public void markMessagesAsRead(Integer roomId, Integer userId) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findByChatRoomIdxOrderByChatSendDateAsc(roomId)
                .stream()
                .filter(message -> !message.isRead() && !message.getSenderUserIdx().equals(userId))
                .collect(Collectors.toList());
        
        unreadMessages.forEach(message -> message.setRead(true));
        chatMessageRepository.saveAll(unreadMessages);
        
        // 채팅방의 읽지 않은 메시지 수 초기화
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException("채팅방을 찾을 수 없습니다.", "CHAT_ROOM_NOT_FOUND"));
        chatRoom.setUnreadCount(0);
        chatRoomRepository.save(chatRoom);
    }
    
    // 채팅방 및 모든 메시지 삭제
    @Transactional
    public void deleteChatRoom(Integer roomId) {
        // 채팅방의 모든 메시지 삭제
        chatMessageRepository.deleteByChatRoomIdx(roomId);
        
        // 채팅방 삭제
        chatRoomRepository.deleteById(roomId);
    }
    
    // 거래 상태 업데이트 메시지 전송
    @Transactional
    public void sendTransactionStatusUpdate(Integer transactionId, String status) {
        // 해당 거래에 대한 채팅방 찾기
        ChatRoom chatRoom = chatRoomRepository.findByTransactionIdx(transactionId)
                .orElseThrow(() -> new ChatException("해당 거래에 대한 채팅방이 없습니다.", "CHAT_ROOM_NOT_FOUND"));
        
        // 상태 업데이트에 대한 시스템 메시지 생성
        String statusMessage = "거래 상태가 '" + status + "'로 변경되었습니다.";
        
        ChatMessage systemMessage = ChatMessage.builder()
                .chatRoomIdx(chatRoom.getChatRoomIdx())
                .senderUserIdx(0) // 시스템 메시지
                .senderName("System")
                .chatMsgContent(statusMessage)
                .chatSendDate(LocalDateTime.now())
                .isRead(false)
                .type(ChatMessage.MessageType.TRANSACTION_UPDATE)
                .build();
        
        ChatMessage savedMessage = chatMessageRepository.save(systemMessage);
        
        // 채팅방 최신 메시지로 업데이트
        chatRoom.setLastMessage(statusMessage);
        chatRoom.setLastMessageTime(savedMessage.getChatSendDate());
        chatRoom.setUnreadCount(chatRoom.getUnreadCount() + 1);
        chatRoomRepository.save(chatRoom);
    }
}

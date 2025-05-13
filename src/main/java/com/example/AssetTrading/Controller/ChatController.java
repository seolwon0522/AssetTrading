package com.example.AssetTrading.Controller;

import com.example.AssetTrading.Dto.ChatMessageRequestDto;
import com.example.AssetTrading.Dto.ChatMessageResponseDto;
import com.example.AssetTrading.Dto.ChatRoomRequestDto;
import com.example.AssetTrading.Dto.ChatRoomResponseDto;
import com.example.AssetTrading.Entity.ChatMessage;
import com.example.AssetTrading.Service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {
    
    private final ChatService chatService;
    
    // REST API 엔드포인트
    
    // 새 채팅방 생성
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponseDto> createChatRoom(@RequestBody ChatRoomRequestDto requestDto) {
        return ResponseEntity.ok(chatService.createChatRoom(requestDto));
    }
    
    // ID로 채팅방 조회
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomResponseDto> getChatRoom(@PathVariable Integer roomId) {
        return ResponseEntity.ok(chatService.getChatRoom(roomId));
    }
    
    // 사용자별 채팅방 조회
    @GetMapping("/rooms/user/{userId}")
    public ResponseEntity<List<ChatRoomResponseDto>> getChatRoomsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(chatService.getChatRoomsByUser(userId));
    }
    
    // 거래 ID로 채팅방 조회
    @GetMapping("/rooms/transaction/{transactionId}")
    public ResponseEntity<ChatRoomResponseDto> getChatRoomByTransaction(@PathVariable Integer transactionId) {
        return ResponseEntity.ok(chatService.getChatRoomByTransaction(transactionId));
    }
    
    // 채팅방의 메시지 페이징 조회
    @GetMapping("/messages/{roomId}")
    public ResponseEntity<Page<ChatMessageResponseDto>> getMessages(
            @PathVariable Integer roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(chatService.getMessages(roomId, page, size));
    }
    
    // 메시지를 읽음으로 표시
    @PutMapping("/messages/{roomId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Integer roomId,
            @RequestParam Integer userId) {
        chatService.markMessagesAsRead(roomId, userId);
        return ResponseEntity.ok().build();
    }
    
    // 채팅방 및 모든 메시지 삭제
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable Integer roomId) {
        chatService.deleteChatRoom(roomId);
        return ResponseEntity.ok().build();
    }
    
    // 메시지 전송 (REST API)
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageResponseDto> sendMessage(@RequestBody ChatMessageRequestDto requestDto) {
        return ResponseEntity.ok(chatService.sendMessage(requestDto));
    }
    
    // 사용자 참여 메시지 생성
    @PostMapping("/join/{roomId}")
    public ResponseEntity<ChatMessageResponseDto> addUser(
            @PathVariable Integer roomId,
            @RequestBody ChatMessageRequestDto requestDto) {
        requestDto.setChatRoomIdx(roomId);
        requestDto.setType(ChatMessage.MessageType.JOIN);
        requestDto.setChatMsgContent(requestDto.getSenderName() + "님이 입장하셨습니다.");
        return ResponseEntity.ok(chatService.sendMessage(requestDto));
    }
    
    // 거래 상태 업데이트 전송
    @PutMapping("/transaction/{transactionId}/status")
    public ResponseEntity<Void> sendTransactionStatusUpdate(
            @PathVariable Integer transactionId,
            @RequestParam String status) {
        chatService.sendTransactionStatusUpdate(transactionId, status);
        return ResponseEntity.ok().build();
    }
}

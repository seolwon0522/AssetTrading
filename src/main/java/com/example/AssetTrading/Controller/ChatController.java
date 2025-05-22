package com.example.AssetTrading.Controller;

import com.example.AssetTrading.Dto.ChatMessageRequestDto;
import com.example.AssetTrading.Dto.ChatMessageResponseDto;
import com.example.AssetTrading.Dto.ChatRoomRequestDto;
import com.example.AssetTrading.Dto.ChatRoomResponseDto;
import com.example.AssetTrading.Entity.ChatMessage;
import com.example.AssetTrading.Exception.ChatException;
import com.example.AssetTrading.Exception.ResourceNotFoundException;
import com.example.AssetTrading.Service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 채팅 기능을 위한 REST API 컨트롤러
 * 채팅방 생성, 메시지 전송, 조회 등의 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Validated
public class ChatController {
    
    private final ChatService chatService;
    
    /**
     * 새 채팅방 생성 API
     * 
     * @param requestDto 채팅방 생성 정보
     * @return 생성된 채팅방 정보
     */
    @PostMapping("/rooms")
    public ResponseEntity<Object> createChatRoom(@RequestBody @Validated ChatRoomRequestDto requestDto) {
        log.info("채팅방 생성 요청: transactionId={}, buyerId={}, sellerId={}", 
                requestDto.getTransaction_idx(), requestDto.getBuyer_user_idx(), requestDto.getSeller_user_idx());
        
        try {
            ChatRoomResponseDto responseDto = chatService.createChatRoom(requestDto);
            log.info("채팅방 생성 완료: roomId={}", responseDto.getChatRoomIdx());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (ChatException e) {
            log.warn("채팅방 생성 실패: errorCode={}, message={}", e.getErrorCode(), e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getErrorCode(), e.getMessage());
        } catch (ResourceNotFoundException e) {
            log.warn("채팅방 생성 실패 - 리소스 없음: {}", e.getMessage());
            return createErrorResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            log.error("채팅방 생성 중 오류 발생: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "채팅방 생성 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * ID로 채팅방 조회 API
     * 
     * @param roomId 채팅방 ID
     * @return 채팅방 정보
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<Object> getChatRoom(@PathVariable Integer roomId) {
        log.info("채팅방 조회 요청: roomId={}", roomId);
        
        try {
            ChatRoomResponseDto responseDto = chatService.getChatRoom(roomId);
            return ResponseEntity.ok(responseDto);
        } catch (ChatException e) {
            log.warn("채팅방 조회 실패: roomId={}, errorCode={}, message={}", roomId, e.getErrorCode(), e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("채팅방 조회 중 오류 발생: roomId={}, error={}", roomId, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "채팅방 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 사용자별 채팅방 조회 API
     * 
     * @param userId 사용자 ID
     * @return 사용자의 채팅방 목록
     */
    @GetMapping("/rooms/user/{userId}")
    public ResponseEntity<Object> getChatRoomsByUser(@PathVariable String userId) {
        log.info("사용자별 채팅방 조회 요청: userId={}", userId);
        
        try {
            List<ChatRoomResponseDto> rooms = chatService.getChatRoomsByUser(userId);
            log.info("사용자별 채팅방 조회 완료: userId={}, count={}", userId, rooms.size());
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            log.error("사용자별 채팅방 조회 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "사용자별 채팅방 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래 ID로 채팅방 조회 API
     * 
     * @param transactionId 거래 ID
     * @return 거래에 해당하는 채팅방 정보
     */
    @GetMapping("/rooms/transaction/{transactionId}")
    public ResponseEntity<Object> getChatRoomByTransaction(@PathVariable Integer transactionId) {
        log.info("거래별 채팅방 조회 요청: transactionId={}", transactionId);
        
        try {
            ChatRoomResponseDto responseDto = chatService.getChatRoomByTransaction(transactionId);
            return ResponseEntity.ok(responseDto);
        } catch (ChatException e) {
            log.warn("거래별 채팅방 조회 실패: transactionId={}, errorCode={}, message={}", 
                    transactionId, e.getErrorCode(), e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("거래별 채팅방 조회 중 오류 발생: transactionId={}, error={}", transactionId, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "거래별 채팅방 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 채팅방의 메시지 페이징 조회 API
     * 
     * @param roomId 채팅방 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 페이징된 메시지 목록
     */
    @GetMapping("/messages/{roomId}")
    public ResponseEntity<Object> getMessages(
            @PathVariable Integer roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("채팅 메시지 페이징 조회 요청: roomId={}, page={}, size={}", roomId, page, size);
        
        try {
            Page<ChatMessageResponseDto> messages = chatService.getMessages(roomId, page, size);
            log.info("채팅 메시지 페이징 조회 완료: roomId={}, totalElements={}, totalPages={}", 
                    roomId, messages.getTotalElements(), messages.getTotalPages());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("채팅 메시지 페이징 조회 중 오류 발생: roomId={}, error={}", roomId, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "채팅 메시지 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 메시지를 읽음으로 표시하는 API
     * 
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 업데이트 결과
     */
    @PutMapping("/messages/{roomId}/read")
    public ResponseEntity<Object> markMessagesAsRead(
            @PathVariable Integer roomId,
            @RequestParam Integer userId) {
        log.info("채팅 메시지 읽음 표시 요청: roomId={}, userId={}", roomId, userId);
        
        try {
            chatService.markMessagesAsRead(roomId, userId);
            log.info("채팅 메시지 읽음 표시 완료: roomId={}, userId={}", roomId, userId);
            return ResponseEntity.ok().build();
        } catch (ChatException e) {
            log.warn("채팅 메시지 읽음 표시 실패: roomId={}, userId={}, errorCode={}, message={}", 
                    roomId, userId, e.getErrorCode(), e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("채팅 메시지 읽음 표시 중 오류 발생: roomId={}, userId={}, error={}", roomId, userId, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "채팅 메시지 읽음 표시 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 채팅방 및 모든 메시지 삭제 API
     * 
     * @param roomId 채팅방 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Object> deleteChatRoom(@PathVariable Integer roomId) {
        log.info("채팅방 삭제 요청: roomId={}", roomId);
        
        try {
            chatService.deleteChatRoom(roomId);
            log.info("채팅방 삭제 완료: roomId={}", roomId);
            return ResponseEntity.ok().build();
        } catch (ChatException e) {
            log.warn("채팅방 삭제 실패: roomId={}, errorCode={}, message={}", roomId, e.getErrorCode(), e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("채팅방 삭제 중 오류 발생: roomId={}, error={}", roomId, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "채팅방 삭제 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 메시지 전송 API
     * 
     * @param requestDto 메시지 정보
     * @return 전송된 메시지 정보
     */
    @PostMapping("/messages")
    public ResponseEntity<Object> sendMessage(@RequestBody @Validated ChatMessageRequestDto requestDto) {
        log.info("메시지 전송 요청: roomId={}, senderId={}", requestDto.getChatRoomIdx(), requestDto.getSenderUserIdx());
        
        try {
            ChatMessageResponseDto responseDto = chatService.sendMessage(requestDto);
            log.info("메시지 전송 완료: messageId={}", responseDto.getChatMsgIdx());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (ChatException e) {
            log.warn("메시지 전송 실패: errorCode={}, message={}", e.getErrorCode(), e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("메시지 전송 중 오류 발생: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "메시지 전송 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 사용자 참여 메시지 생성 API
     * 
     * @param roomId 채팅방 ID
     * @param requestDto 메시지 정보
     * @return 생성된 참여 메시지 정보
     */
    @PostMapping("/join/{roomId}")
    public ResponseEntity<Object> addUser(
            @PathVariable Integer roomId,
            @RequestBody ChatMessageRequestDto requestDto) {
        log.info("사용자 참여 메시지 생성 요청: roomId={}, senderName={}", roomId, requestDto.getSenderName());
        
        try {
            requestDto.setChatRoomIdx(roomId);
            requestDto.setType(ChatMessage.MessageType.JOIN);
            requestDto.setChatMsgContent(requestDto.getSenderName() + "님이 입장하셨습니다.");
            
            ChatMessageResponseDto responseDto = chatService.sendMessage(requestDto);
            log.info("사용자 참여 메시지 생성 완료: roomId={}, messageId={}", roomId, responseDto.getChatMsgIdx());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (ChatException e) {
            log.warn("사용자 참여 메시지 생성 실패: roomId={}, errorCode={}, message={}", roomId, e.getErrorCode(), e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("사용자 참여 메시지 생성 중 오류 발생: roomId={}, error={}", roomId, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "사용자 참여 메시지 생성 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래 상태 업데이트 전송 API
     * 
     * @param transactionId 거래 ID
     * @param status 거래 상태
     * @return 업데이트 결과
     */
    @PutMapping("/transaction/{transactionId}/status")
    public ResponseEntity<Object> sendTransactionStatusUpdate(
            @PathVariable Integer transactionId,
            @RequestParam String status) {
        log.info("거래 상태 업데이트 메시지 전송 요청: transactionId={}, status={}", transactionId, status);
        
        try {
            chatService.sendTransactionStatusUpdate(transactionId, status);
            log.info("거래 상태 업데이트 메시지 전송 완료: transactionId={}, status={}", transactionId, status);
            return ResponseEntity.ok().build();
        } catch (ChatException e) {
            log.warn("거래 상태 업데이트 메시지 전송 실패: transactionId={}, status={}, errorCode={}, message={}", 
                    transactionId, status, e.getErrorCode(), e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getErrorCode(), e.getMessage());
        } catch (ResourceNotFoundException e) {
            log.warn("거래 상태 업데이트 메시지 전송 실패 - 리소스 없음: transactionId={}, error={}", transactionId, e.getMessage());
            return createErrorResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            log.error("거래 상태 업데이트 메시지 전송 중 오류 발생: transactionId={}, status={}, error={}", 
                    transactionId, status, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "거래 상태 업데이트 메시지 전송 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 에러 응답 생성 헬퍼 메소드
     */
    private ResponseEntity<Object> createErrorResponse(HttpStatus status, String errorCode, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", java.time.LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("errorCode", errorCode);
        body.put("message", message);
        
        return ResponseEntity.status(status).body(body);
    }
    
    /**
     * 전역 예외 처리기
     */
    @ExceptionHandler(ChatException.class)
    public ResponseEntity<Object> handleChatException(ChatException e) {
        log.warn("채팅 오류: errorCode={}, message={}", e.getErrorCode(), e.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, e.getErrorCode(), e.getMessage());
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("리소스를 찾을 수 없음: {}", e.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", e.getMessage());
    }
}

package com.example.AssetTrading.Controller;

import com.example.AssetTrading.Dto.ChatMessageRequestDto;
import com.example.AssetTrading.Dto.ChatMessageResponseDto;
import com.example.AssetTrading.Dto.ChatRoomRequestDto;
import com.example.AssetTrading.Dto.ChatRoomResponseDto;
import com.example.AssetTrading.Service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    private ChatRoomRequestDto chatRoomRequestDto;
    private ChatRoomResponseDto chatRoomResponseDto;
    private ChatMessageRequestDto chatMessageRequestDto;
    private ChatMessageResponseDto chatMessageResponseDto;

    @BeforeEach
    void setUp() {
        // 채팅방 요청 DTO 설정
        chatRoomRequestDto = ChatRoomRequestDto.builder()
                .transaction_idx(1)
                .buyer_user_idx(1L)
                .buyer_user_id("buyer1")
                .buyerName("구매자")
                .seller_user_idx(2L)
                .seller_user_id("seller1")
                .sellerName("판매자")
                .product_idx(1L)
                .productTitle("테스트 상품")
                .build();

        // 채팅방 응답 DTO 설정
        chatRoomResponseDto = ChatRoomResponseDto.builder()
                .chatRoomIdx(1)
                .transactionIdx(1)
                .buyerUserIdx(1L)
                .buyerUserId("buyer1")
                .buyerName("구매자")
                .sellerUserIdx(2L)
                .sellerUserId("seller1")
                .sellerName("판매자")
                .productIdx(1L)
                .productTitle("테스트 상품")
                .lastMessage("안녕하세요")
                .lastMessageTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        // 채팅 메시지 요청 DTO 설정
        chatMessageRequestDto = ChatMessageRequestDto.builder()
                .chatRoomIdx(1)
                .senderUserIdx(1L)
                .senderName("구매자")
                .content("안녕하세요")
                .build();

        // 채팅 메시지 응답 DTO 설정
        chatMessageResponseDto = ChatMessageResponseDto.builder()
                .chatMsgIdx(1L)
                .chatRoomIdx(1)
                .senderUserIdx(1L)
                .senderName("구매자")
                .content("안녕하세요")
                .messageTime(LocalDateTime.now())
                .isRead(false)
                .build();
    }

    @Test
    @DisplayName("채팅방 생성 성공 테스트")
    void createChatRoomSuccess() throws Exception {
        // Given
        when(chatService.createChatRoom(any(ChatRoomRequestDto.class))).thenReturn(chatRoomResponseDto);

        // When & Then
        mockMvc.perform(post("/api/chat/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatRoomRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chatRoomIdx").value(1))
                .andExpect(jsonPath("$.buyerUserId").value("buyer1"))
                .andExpect(jsonPath("$.sellerUserId").value("seller1"));
    }

    @Test
    @DisplayName("사용자별 채팅방 목록 조회 테스트")
    void getChatRoomsByUserSuccess() throws Exception {
        // Given
        List<ChatRoomResponseDto> chatRooms = Arrays.asList(chatRoomResponseDto);
        when(chatService.getChatRoomsByUser(anyString())).thenReturn(chatRooms);

        // When & Then
        mockMvc.perform(get("/api/chat/rooms/user/buyer1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chatRoomIdx").value(1))
                .andExpect(jsonPath("$[0].buyerUserIdx").value(1));
    }

    @Test
    @DisplayName("채팅방 상세 조회 테스트")
    void getChatRoomDetailSuccess() throws Exception {
        // Given
        when(chatService.getChatRoom(anyInt())).thenReturn(chatRoomResponseDto);

        // When & Then
        mockMvc.perform(get("/api/chat/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatRoomIdx").value(1))
                .andExpect(jsonPath("$.productTitle").value("테스트 상품"));
    }

    @Test
    @DisplayName("거래별 채팅방 조회 테스트")
    void getChatRoomByTransactionSuccess() throws Exception {
        // Given
        when(chatService.getChatRoomByTransaction(anyInt())).thenReturn(chatRoomResponseDto);

        // When & Then
        mockMvc.perform(get("/api/chat/rooms/transaction/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionIdx").value(1));
    }

    @Test
    @DisplayName("채팅 메시지 전송 테스트")
    void sendMessageSuccess() throws Exception {
        // Given
        when(chatService.sendMessage(any(ChatMessageRequestDto.class))).thenReturn(chatMessageResponseDto);

        // When & Then
        mockMvc.perform(post("/api/chat/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatMessageRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chatMsgIdx").value(1))
                .andExpect(jsonPath("$.content").value("안녕하세요"));
    }

    @Test
    @DisplayName("채팅방 메시지 목록 조회 테스트")
    void getChatMessagesSuccess() throws Exception {
        // Given
        List<ChatMessageResponseDto> messages = Arrays.asList(chatMessageResponseDto);
        when(chatService.getMessages(anyInt(), anyInt(), anyInt())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/chat/messages/1")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("채팅 메시지 읽음 처리 테스트")
    void markMessagesAsReadSuccess() throws Exception {
        // Given
        // When & Then
        mockMvc.perform(put("/api/chat/messages/1/read")
                .param("userId", "2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("채팅방 삭제 테스트")
    void deleteChatRoomSuccess() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/chat/rooms/1"))
                .andExpect(status().isOk());
    }
} 
package com.example.AssetTrading.Controller;

import com.example.AssetTrading.Dto.TransactionRequestDto;
import com.example.AssetTrading.Dto.TransactionResponseDto;
import com.example.AssetTrading.Entity.TransactionStatus;
import com.example.AssetTrading.Exception.ResourceNotFoundException;
import com.example.AssetTrading.Service.TransactionService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    private TransactionRequestDto transactionRequestDto;
    private TransactionResponseDto transactionResponseDto;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 설정
        transactionRequestDto = new TransactionRequestDto();
        transactionRequestDto.setBuyerId(1L);
        transactionRequestDto.setProductId(2L);

        transactionResponseDto = TransactionResponseDto.builder()
                .transaction_idx(1L)
                .product_idx(2L)
                .productTitle("테스트 상품")
                .productPrice(10000)
                .productImg("test.jpg")
                .buyer_user_idx(1L)
                .buyerId("buyer1")
                .buyerName("구매자")
                .seller_user_idx(3L)
                .sellerId("seller1")
                .sellerName("판매자")
                .status(TransactionStatus.REQUESTED)
                .created_at(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("거래 요청 성공 테스트")
    void requestTransactionSuccess() throws Exception {
        // Given
        when(transactionService.requestTransaction(any(TransactionRequestDto.class))).thenReturn(transactionResponseDto);

        // When & Then
        mockMvc.perform(post("/api/transactions/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transaction_idx").value(1))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    @DisplayName("거래 요청 실패 - 리소스 없음 테스트")
    void requestTransactionFailResourceNotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("존재하지 않는 상품입니다."))
                .when(transactionService).requestTransaction(any(TransactionRequestDto.class));

        // When & Then
        mockMvc.perform(post("/api/transactions/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("존재하지 않는 상품입니다."));
    }

    @Test
    @DisplayName("거래 승인 성공 테스트")
    void processTransactionSuccess() throws Exception {
        // Given
        transactionResponseDto.setStatus(TransactionStatus.PROCESSING);
        when(transactionService.processTransaction(anyLong())).thenReturn(transactionResponseDto);

        // When & Then
        mockMvc.perform(put("/api/transactions/1/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @DisplayName("거래 완료 성공 테스트")
    void completeTransactionSuccess() throws Exception {
        // Given
        transactionResponseDto.setStatus(TransactionStatus.COMPLETED);
        when(transactionService.completeTransaction(anyLong())).thenReturn(transactionResponseDto);

        // When & Then
        mockMvc.perform(put("/api/transactions/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("거래 취소 성공 테스트")
    void cancelTransactionSuccess() throws Exception {
        // Given
        transactionResponseDto.setStatus(TransactionStatus.CANCELED);
        when(transactionService.cancelTransaction(anyLong())).thenReturn(transactionResponseDto);

        // When & Then
        mockMvc.perform(put("/api/transactions/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    @DisplayName("거래 상세 조회 성공 테스트")
    void getTransactionSuccess() throws Exception {
        // Given
        when(transactionService.getTransaction(anyLong())).thenReturn(transactionResponseDto);

        // When & Then
        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction_idx").value(1));
    }

    @Test
    @DisplayName("구매자별 거래 내역 조회 테스트")
    void getTransactionsByBuyerSuccess() throws Exception {
        // Given
        List<TransactionResponseDto> transactions = Arrays.asList(transactionResponseDto);
        when(transactionService.getTransactionsByBuyer(anyLong())).thenReturn(transactions);

        // When & Then
        mockMvc.perform(get("/api/transactions/buyer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].buyer_user_idx").value(1));
    }

    @Test
    @DisplayName("판매자별 거래 내역 조회 테스트")
    void getTransactionsBySellerSuccess() throws Exception {
        // Given
        List<TransactionResponseDto> transactions = Arrays.asList(transactionResponseDto);
        when(transactionService.getTransactionsBySeller(anyLong())).thenReturn(transactions);

        // When & Then
        mockMvc.perform(get("/api/transactions/seller/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seller_user_idx").value(3));
    }

    @Test
    @DisplayName("상품별 거래 내역 조회 테스트")
    void getTransactionsByProductSuccess() throws Exception {
        // Given
        List<TransactionResponseDto> transactions = Arrays.asList(transactionResponseDto);
        when(transactionService.getTransactionsByProduct(anyLong())).thenReturn(transactions);

        // When & Then
        mockMvc.perform(get("/api/transactions/product/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].product_idx").value(2));
    }

    @Test
    @DisplayName("거래 상태별 조회 테스트")
    void getTransactionsByStatusSuccess() throws Exception {
        // Given
        List<TransactionResponseDto> transactions = Arrays.asList(transactionResponseDto);
        when(transactionService.getTransactionsByStatus(any(TransactionStatus.class))).thenReturn(transactions);

        // When & Then
        mockMvc.perform(get("/api/transactions/status/REQUESTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("REQUESTED"));
    }

    @Test
    @DisplayName("모든 거래 조회 테스트")
    void getAllTransactionsSuccess() throws Exception {
        // Given
        List<TransactionResponseDto> transactions = Arrays.asList(
                transactionResponseDto, 
                TransactionResponseDto.builder()
                        .transaction_idx(2L)
                        .status(TransactionStatus.COMPLETED)
                        .build()
        );
        when(transactionService.getAllTransactions()).thenReturn(transactions);

        // When & Then
        mockMvc.perform(get("/api/transactions/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transaction_idx").value(1))
                .andExpect(jsonPath("$[1].transaction_idx").value(2));
    }
} 
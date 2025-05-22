package com.example.AssetTrading.Controller;

import com.example.AssetTrading.Service.BusinessNumCheckService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BusinessVerificationController.class)
public class BusinessVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BusinessNumCheckService businessNumCheckService;

    private Map<String, Object> requestMap;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 설정
        requestMap = new HashMap<>();
        requestMap.put("businessNum", "1234567890");
        requestMap.put("startDate", "20200101");
        requestMap.put("companyName", "테스트 회사");
        requestMap.put("userName", "홍길동");
    }

    @Test
    @DisplayName("사업자 번호 검증 성공 테스트")
    void verifyBusinessNumSuccess() throws Exception {
        // Given
        when(businessNumCheckService.checkBusinessNum(
                anyString(), anyString(), anyString(), anyString()
        )).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/business-verification/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status_code").value("OK"));
    }

    @Test
    @DisplayName("사업자 번호 검증 실패 테스트")
    void verifyBusinessNumFail() throws Exception {
        // Given
        when(businessNumCheckService.checkBusinessNum(
                anyString(), anyString(), anyString(), anyString()
        )).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/business-verification/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid_cnt").value("0"));
    }

    @Test
    @DisplayName("사업자 번호 검증 실패 - 입력값 누락 테스트")
    void verifyBusinessNumInvalidInput() throws Exception {
        // Given
        requestMap.remove("businessNum");

        // When & Then
        mockMvc.perform(post("/api/business-verification/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMap)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("사업자 번호 검증 서비스 오류 테스트")
    void verifyBusinessNumServiceError() throws Exception {
        // Given
        when(businessNumCheckService.checkBusinessNum(
                anyString(), anyString(), anyString(), anyString()
        )).thenThrow(new RuntimeException("외부 API 호출 중 오류가 발생했습니다."));

        // When & Then
        mockMvc.perform(post("/api/business-verification/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMap)))
                .andExpect(status().isInternalServerError());
    }
} 
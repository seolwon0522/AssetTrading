package com.example.AssetTrading.Controller;

import com.example.AssetTrading.Dto.SellProductRequestDto;
import com.example.AssetTrading.Dto.SellProductResponseDto;
import com.example.AssetTrading.Dto.UserResponseDto;
import com.example.AssetTrading.Entity.ProductStatus;
import com.example.AssetTrading.Service.SellProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SellProductController.class)
public class SellProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SellProductService sellProductService;

    private SellProductRequestDto requestDto;
    private SellProductResponseDto responseDto;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 설정
        requestDto = SellProductRequestDto.builder()
                .productTitle("테스트 상품")
                .productDesc("테스트 상품 설명")
                .productImg("test.jpg")
                .productPrice(10000)
                .productQuantity(5)
                .productCreateDate("2023-06-01")
                .productAvailDate(LocalDate.now())
                .sellerUserIdx(1L)
                .productStatus("AVAILABLE")
                .productCategory("전자기기")
                .productTags("테스트,상품,전자기기")
                .featured(false)
                .build();

        responseDto = SellProductResponseDto.builder()
                .id(1L)
                .productTitle("테스트 상품")
                .productDesc("테스트 상품 설명")
                .productImg("test.jpg")
                .productPrice(10000)
                .productQuantity(5)
                .productCreateDate("2023-06-01")
                .productAvailDate(LocalDate.now())
                .sellerUserIdx(1L)
                .productStatus("AVAILABLE")
                .productCategory("전자기기")
                .productTags("테스트,상품,전자기기")
                .viewCount(0)
                .featured(false)
                .build();

        session = new MockHttpSession();
        session.setAttribute("LOGIN_USER", UserResponseDto.builder()
                .userId("testuser")
                .companyName("테스트 회사")
                .joinApproved(true)
                .build());
    }

    @Test
    @DisplayName("상품 등록 성공 테스트")
    void createProductSuccess() throws Exception {
        // Given
        when(sellProductService.createSellProductWithSession(any(SellProductRequestDto.class), any())).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .session(session))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productTitle").value("테스트 상품"))
                .andExpect(jsonPath("$.productStatus").value("AVAILABLE"));
    }

    @Test
    @DisplayName("상품 상세 정보 조회 성공 테스트")
    void getProductDetailSuccess() throws Exception {
        // Given
        when(sellProductService.getSellProductById(anyLong())).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productTitle").value("테스트 상품"));
    }

    @Test
    @DisplayName("상품 목록 조회 성공 테스트")
    void getAllProductsSuccess() throws Exception {
        // Given
        List<SellProductResponseDto> products = Arrays.asList(responseDto);
        when(sellProductService.getAllSellProducts(anyString())).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                .param("keyword", "테스트"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productTitle").value("테스트 상품"));
    }

    @Test
    @DisplayName("페이지네이션된 상품 목록 조회 성공 테스트")
    void getAllProductsPagedSuccess() throws Exception {
        // Given
        Page<SellProductResponseDto> productPage = new PageImpl<>(Arrays.asList(responseDto));
        when(sellProductService.getAllSellProductsPaged(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(productPage);

        // When & Then
        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10")
                .param("keyword", "테스트")
                .param("sort", "id")
                .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productTitle").value("테스트 상품"));
    }

    @Test
    @DisplayName("상품 수정 성공 테스트")
    void updateProductSuccess() throws Exception {
        // Given
        SellProductResponseDto updatedDto = SellProductResponseDto.builder()
                .id(1L)
                .productTitle("수정된 상품")
                .productPrice(15000)
                .productStatus("AVAILABLE")
                .build();
        
        when(sellProductService.updateSellProduct(anyLong(), any(SellProductRequestDto.class))).thenReturn(updatedDto);

        // When & Then
        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productTitle").value("수정된 상품"))
                .andExpect(jsonPath("$.productPrice").value(15000));
    }

    @Test
    @DisplayName("상품 상태 변경 성공 테스트")
    void updateProductStatusSuccess() throws Exception {
        // Given
        mockMvc.perform(patch("/api/products/1/status")
                .param("status", "UNAVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("상품 상태가 업데이트되었습니다."));
    }

    @Test
    @DisplayName("상품 삭제 성공 테스트")
    void deleteProductSuccess() throws Exception {
        // Given
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("상품이 삭제되었습니다."));
    }

    @Test
    @DisplayName("구매 가능한 상품 목록 조회 성공 테스트")
    void getAvailableProductsSuccess() throws Exception {
        // Given
        List<SellProductResponseDto> products = Arrays.asList(responseDto);
        when(sellProductService.getVisibleProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productStatus").value("AVAILABLE"));
    }

    @Test
    @DisplayName("페이지네이션된 구매 가능한 상품 목록 조회 성공 테스트")
    void getAvailableProductsPagedSuccess() throws Exception {
        // Given
        Page<SellProductResponseDto> productPage = new PageImpl<>(Arrays.asList(responseDto));
        when(sellProductService.getVisibleProductsPaged(anyInt(), anyInt())).thenReturn(productPage);

        // When & Then
        mockMvc.perform(get("/api/products/available/paged")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productStatus").value("AVAILABLE"));
    }

    @Test
    @DisplayName("카테고리별 상품 조회 성공 테스트")
    void getProductsByCategorySuccess() throws Exception {
        // Given
        List<SellProductResponseDto> products = Arrays.asList(responseDto);
        when(sellProductService.getProductsByCategory(anyString())).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products/category/전자기기"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productCategory").value("전자기기"));
    }

    @Test
    @DisplayName("추천 상품 목록 조회 성공 테스트")
    void getFeaturedProductsSuccess() throws Exception {
        // Given
        responseDto.setFeatured(true);
        List<SellProductResponseDto> products = Arrays.asList(responseDto);
        when(sellProductService.getFeaturedProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].featured").value(true));
    }
} 
package com.example.AssetTrading.Controller;

import com.example.AssetTrading.Dto.SellProductRequestDto;
import com.example.AssetTrading.Dto.SellProductResponseDto;
import com.example.AssetTrading.Service.SellProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class SellProductController {

    private final SellProductService sellProductService;

    // 새 판매 상품 등록(기능 구현 완료)
    @PostMapping("/register/session")
    public ResponseEntity<SellProductResponseDto> createSellProduct(@RequestBody SellProductRequestDto requestDto,
                                                                HttpSession session) {
        SellProductResponseDto responseDto = sellProductService.createSellProductWithSession(requestDto, session);
        return ResponseEntity.ok(responseDto);
    }

    // 키워드로 상품 목록을 조회 (기능 구현 완료)
    @GetMapping("/search")
    public ResponseEntity<List<SellProductResponseDto>> getAllSellProducts(@RequestParam String keyword) {
        List<SellProductResponseDto> products = sellProductService.getAllSellProducts(keyword);
        return ResponseEntity.ok(products);
    }
    
    // 키워드로 상품 목록을 페이징하여 조회
    @GetMapping("/search/paged")
    public ResponseEntity<Page<SellProductResponseDto>> getAllSellProductsPaged(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        Page<SellProductResponseDto> products = sellProductService.getAllSellProductsPaged(
                keyword, page, size, sortBy, direction);
        return ResponseEntity.ok(products);
    }

    // 상품 ID로 특정 판매 상품정보를 조회 (기능 구현 완료)
    @GetMapping("/{productId}")
    public ResponseEntity<SellProductResponseDto> getSellProductById(@PathVariable Long productId) {
        SellProductResponseDto responseDto = sellProductService.getSellProductById(productId);
        return ResponseEntity.ok(responseDto);
    }

    // 상품 ID로 특정 판매 상품을 삭제 (기능 구현 완료)
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteSellProduct(@PathVariable Long productId) {
        sellProductService.deleteSellProduct(productId);
        return ResponseEntity.noContent().build();
    }
    
    // 상품 정보 업데이트
    @PutMapping("/{productId}")
    public ResponseEntity<SellProductResponseDto> updateSellProduct(
            @PathVariable Long productId,
            @RequestBody SellProductRequestDto requestDto) {
        SellProductResponseDto responseDto = sellProductService.updateSellProduct(productId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    // 상품 상태 업데이트 (기능 구현 완료)
    @PutMapping("/status/{productId}")
    public ResponseEntity<Void> updateProductStatus(@PathVariable Long productId,
                                                    @RequestParam boolean isSellingAvailable) {
        sellProductService.updateStatus(productId, isSellingAvailable);
        return ResponseEntity.ok().build();
    }
    
    // 상품 상태 상세 업데이트 (AVAILABLE, UNAVAILABLE, RESERVED, SOLD_OUT)
    @PutMapping("/status/detail/{productId}")
    public ResponseEntity<Void> updateDetailProductStatus(
            @PathVariable Long productId,
            @RequestParam String status) {
        sellProductService.updateProductStatus(productId, status);
        return ResponseEntity.ok().build();
    }

    // 노출 가능 상품 목록 조회 (기능 구현 완료)
    @GetMapping("/visible")
    public ResponseEntity<List<SellProductResponseDto>> getVisibleProducts() {
        List<SellProductResponseDto> visibleProducts = sellProductService.getVisibleProducts();
        return ResponseEntity.ok(visibleProducts);
    }
    
    // 노출 가능 상품 목록 페이징 조회
    @GetMapping("/visible/paged")
    public ResponseEntity<Page<SellProductResponseDto>> getVisibleProductsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SellProductResponseDto> visibleProducts = sellProductService.getVisibleProductsPaged(page, size);
        return ResponseEntity.ok(visibleProducts);
    }
    
    // 카테고리별 상품 조회
    @GetMapping("/category/{category}")
    public ResponseEntity<List<SellProductResponseDto>> getProductsByCategory(@PathVariable String category) {
        List<SellProductResponseDto> products = sellProductService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }
    
    // 판매자별 상품 조회
    @GetMapping("/seller/{sellerUserIdx}")
    public ResponseEntity<List<SellProductResponseDto>> getProductsBySellerUserIdx(@PathVariable Long sellerUserIdx) {
        List<SellProductResponseDto> products = sellProductService.getProductsBySellerUserIdx(sellerUserIdx);
        return ResponseEntity.ok(products);
    }
    
    // 추천 상품 조회
    @GetMapping("/featured")
    public ResponseEntity<List<SellProductResponseDto>> getFeaturedProducts() {
        List<SellProductResponseDto> products = sellProductService.getFeaturedProducts();
        return ResponseEntity.ok(products);
    }
    
    // 태그별 상품 조회
    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<SellProductResponseDto>> getProductsByTag(@PathVariable String tag) {
        List<SellProductResponseDto> products = sellProductService.getProductsByTag(tag);
        return ResponseEntity.ok(products);
    }
    
    // 가격 범위로 상품 조회
    @GetMapping("/price-range")
    public ResponseEntity<List<SellProductResponseDto>> getProductsByPriceRange(
            @RequestParam Integer minPrice,
            @RequestParam Integer maxPrice) {
        List<SellProductResponseDto> products = sellProductService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }
    
    // 인기 상품 조회 (조회수 기준)
    @GetMapping("/popular")
    public ResponseEntity<List<SellProductResponseDto>> getTopViewedProducts() {
        List<SellProductResponseDto> products = sellProductService.getTopViewedProducts();
        return ResponseEntity.ok(products);
    }
    
    // 추천 상품 토글 (추천 설정/해제)
    @PutMapping("/featured/toggle/{productId}")
    public ResponseEntity<Void> toggleFeaturedStatus(@PathVariable Long productId) {
        sellProductService.toggleFeaturedStatus(productId);
        return ResponseEntity.ok().build();
    }
    
    // 에러 핸들링
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", e.getMessage()));
    }
}
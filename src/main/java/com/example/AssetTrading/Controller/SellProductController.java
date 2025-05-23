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
@RequestMapping("/api/products")
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
    public ResponseEntity<List<SellProductResponseDto>> getAllSellProducts(@RequestParam("keyword") String keyword) {
        List<SellProductResponseDto> products = sellProductService.getAllSellProducts(keyword);
        return ResponseEntity.ok(products);
    }

    // 키워드로 상품 목록을 페이징하여 조회 (나중에 pagenation 기능은 제외 예정)
    @GetMapping("/search/paged")
    public ResponseEntity<Page<SellProductResponseDto>> getAllSellProductsPaged(
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "direction", defaultValue = "ASC") String direction) {
        Page<SellProductResponseDto> products = sellProductService.getAllSellProductsPaged(
                keyword, page, size, sortBy, direction);
        return ResponseEntity.ok(products);
    }

    // 상품 ID로 특정 판매 상품정보를 조회 (기능 구현 완료)
    @GetMapping("/list/{productId}")
    public ResponseEntity<SellProductResponseDto> getSellProductById(@PathVariable("productId") Long productId) {
        SellProductResponseDto responseDto = sellProductService.getSellProductById(productId);
        return ResponseEntity.ok(responseDto);
    }

    // 상품 ID로 특정 판매 상품을 삭제 (기능 구현 완료) (추후 PutMapping으로 변경하여 명시적 삭제 방식으로 수정하기)
    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<Void> deleteSellProduct(@PathVariable("productId") Long productId) {
        sellProductService.deleteSellProduct(productId);
        return ResponseEntity.noContent().build();
    }

    // 상품 정보 업데이트
    @PutMapping("/update/{productId}")
    public ResponseEntity<SellProductResponseDto> updateSellProduct(
            @PathVariable("productId") Long productId,
            @RequestBody SellProductRequestDto requestDto) {
        SellProductResponseDto responseDto = sellProductService.updateSellProduct(productId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    // 상품 상태 업데이트 (기능 구현 완료)
    @PutMapping("/update/status/{productId}")
    public ResponseEntity<Void> updateProductStatus(@PathVariable("productId") Long productId,
                                                    @RequestParam("isSellingAvailable") boolean isSellingAvailable) {
        sellProductService.updateStatus(productId, isSellingAvailable);
        return ResponseEntity.ok().build();
    }

    // 상품 상태 상세 업데이트 (AVAILABLE, UNAVAILABLE, RESERVED, SOLD_OUT)
    @PutMapping("/update/status/detail/{productId}")
    public ResponseEntity<Void> updateDetailProductStatus(
            @PathVariable("productId") Long productId,
            @RequestParam("status") String status) {
        sellProductService.updateProductStatus(productId, status);
        return ResponseEntity.ok().build();
    }

    // 노출 가능 상품 목록 조회 (기능 구현 완료)
    @GetMapping("/list/visible")
    public ResponseEntity<List<SellProductResponseDto>> getVisibleProducts() {
        List<SellProductResponseDto> visibleProducts = sellProductService.getVisibleProducts();
        return ResponseEntity.ok(visibleProducts);
    }

    // 노출 가능 상품 목록 페이징 조회 (나중에 pagenation 기능은 제외 예정)
    @GetMapping("/visible/paged")
    public ResponseEntity<Page<SellProductResponseDto>> getVisibleProductsPaged(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<SellProductResponseDto> visibleProducts = sellProductService.getVisibleProductsPaged(page, size);
        return ResponseEntity.ok(visibleProducts);
    }

    // 카테고리별 상품 조회
    @GetMapping("/list/category/{category}")
    public ResponseEntity<List<SellProductResponseDto>> getProductsByCategory(@PathVariable("category") String category) {
        List<SellProductResponseDto> products = sellProductService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    // 판매자별 상품 조회
    @GetMapping("/list/seller/{sellerUserIdx}")
    public ResponseEntity<List<SellProductResponseDto>> getProductsBySellerUserIdx(@PathVariable("sellerUserIdx") Long sellerUserIdx) {
        List<SellProductResponseDto> products = sellProductService.getProductsBySellerUserIdx(sellerUserIdx);
        return ResponseEntity.ok(products);
    }

    // 추천 상품 조회
    @GetMapping("/list/featured")
    public ResponseEntity<List<SellProductResponseDto>> getFeaturedProducts() {
        List<SellProductResponseDto> products = sellProductService.getFeaturedProducts();
        return ResponseEntity.ok(products);
    }

    // 태그별 상품 조회
    @GetMapping("/list/tag/{tag}")
    public ResponseEntity<List<SellProductResponseDto>> getProductsByTag(@PathVariable("tag") String tag) {
        List<SellProductResponseDto> products = sellProductService.getProductsByTag(tag);
        return ResponseEntity.ok(products);
    }

    // 가격 범위로 상품 조회
    @GetMapping("/list/price-range")
    public ResponseEntity<List<SellProductResponseDto>> getProductsByPriceRange(
            @RequestParam("minPrice") Integer minPrice,
            @RequestParam("maxPrice") Integer maxPrice) {
        List<SellProductResponseDto> products = sellProductService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    // 인기 상품 조회 (조회수 기준)
    @GetMapping("/list/popular")
    public ResponseEntity<List<SellProductResponseDto>> getTopViewedProducts() {
        List<SellProductResponseDto> products = sellProductService.getTopViewedProducts();
        return ResponseEntity.ok(products);
    }

    // 추천 상품 토글 (추천 설정/해제)
    @PutMapping("/featured/toggle/{productId}")
    public ResponseEntity<Void> toggleFeaturedStatus(@PathVariable("productId") Long productId) {
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
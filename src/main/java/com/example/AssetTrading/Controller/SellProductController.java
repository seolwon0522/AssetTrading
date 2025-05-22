package com.example.AssetTrading.Controller;

import com.example.AssetTrading.Dto.SellProductRequestDto;
import com.example.AssetTrading.Dto.SellProductResponseDto;
import com.example.AssetTrading.Exception.ResourceNotFoundException;
import com.example.AssetTrading.Service.SellProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 판매 상품 관리를 위한 REST API 컨트롤러
 * 상품 등록, 조회, 수정, 삭제 등의 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
@Validated
public class SellProductController {

    private final SellProductService sellProductService;

    /**
     * 새 판매 상품 등록 API
     * 
     * @param requestDto 상품 등록 정보
     * @param session 사용자 세션 정보
     * @return 등록된 상품 정보
     */
    @PostMapping("/register/session")
    public ResponseEntity<Object> createSellProduct(
            @RequestBody @Validated SellProductRequestDto requestDto,
            HttpSession session) {
        log.info("상품 등록 요청: title={}, category={}", requestDto.getProductTitle(), requestDto.getProductCategory());
        
        try {
            SellProductResponseDto responseDto = sellProductService.createSellProductWithSession(requestDto, session);
            log.info("상품 등록 완료: productId={}", responseDto.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IllegalStateException e) {
            log.warn("상품 등록 실패 - 로그인 상태 오류: {}", e.getMessage());
            return createErrorResponse(HttpStatus.UNAUTHORIZED, "AUTH_ERROR", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("상품 등록 실패 - 입력값 오류: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_INPUT", e.getMessage());
        } catch (Exception e) {
            log.error("상품 등록 중 오류 발생: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "상품 등록 중 오류가 발생했습니다.");
        }
    }

    /**
     * 키워드로 상품 목록 조회 API
     * 
     * @param keyword 검색 키워드
     * @return 검색된 상품 목록
     */
    @GetMapping("/search")
    public ResponseEntity<Object> getAllSellProducts(@RequestParam("keyword") String keyword) {
        log.info("상품 검색 요청: keyword={}", keyword);
        
        try {
            List<SellProductResponseDto> products = sellProductService.getAllSellProducts(keyword);
            log.info("상품 검색 결과: keyword={}, count={}", keyword, products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("상품 검색 중 오류 발생: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "상품 검색 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 키워드로 상품 목록을 페이징하여 조회하는 API
     * 
     * @param keyword 검색 키워드
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param sortBy 정렬 기준 필드
     * @param direction 정렬 방향 (ASC, DESC)
     * @return 페이징된 상품 목록
     */
    @GetMapping("/search/paged")
    public ResponseEntity<Object> getAllSellProductsPaged(
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "direction", defaultValue = "ASC") String direction) {
        log.info("페이징 상품 검색 요청: keyword={}, page={}, size={}", keyword, page, size);
        
        try {
            Page<SellProductResponseDto> products = sellProductService.getAllSellProductsPaged(
                    keyword, page, size, sortBy, direction);
            log.info("페이징 상품 검색 결과: keyword={}, totalElements={}, totalPages={}", 
                    keyword, products.getTotalElements(), products.getTotalPages());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("페이징 상품 검색 중 오류 발생: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "상품 검색 중 오류가 발생했습니다.");
        }
    }

    /**
     * 상품 ID로 특정 판매 상품정보를 조회하는 API
     * 
     * @param productId 상품 ID
     * @return 상품 정보
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Object> getSellProductById(@PathVariable("productId") Long productId) {
        log.info("상품 상세 조회 요청: productId={}", productId);
        
        try {
            SellProductResponseDto responseDto = sellProductService.getSellProductById(productId);
            log.info("상품 상세 조회 완료: productId={}, title={}", productId, responseDto.getProductTitle());
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            log.warn("상품 상세 조회 실패 - 상품 없음: productId={}", productId);
            return createErrorResponse(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            log.error("상품 상세 조회 중 오류 발생: productId={}, error={}", productId, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "상품 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 상품 ID로 특정 판매 상품을 삭제하는 API
     * 
     * @param productId 상품 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Object> deleteSellProduct(@PathVariable("productId") Long productId) {
        log.info("상품 삭제 요청: productId={}", productId);
        
        try {
            sellProductService.deleteSellProduct(productId);
            log.info("상품 삭제 완료: productId={}", productId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("상품 삭제 실패 - 상품 없음: productId={}", productId);
            return createErrorResponse(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생: productId={}, error={}", productId, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "상품 삭제 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 상품 정보 업데이트 API
     * 
     * @param productId 상품 ID
     * @param requestDto 업데이트할 상품 정보
     * @return 업데이트된 상품 정보
     */
    @PutMapping("/{productId}")
    public ResponseEntity<Object> updateSellProduct(
            @PathVariable("productId") Long productId,
            @RequestBody @Validated SellProductRequestDto requestDto) {
        log.info("상품 정보 업데이트 요청: productId={}", productId);
        
        try {
            SellProductResponseDto responseDto = sellProductService.updateSellProduct(productId, requestDto);
            log.info("상품 정보 업데이트 완료: productId={}", productId);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            log.warn("상품 정보 업데이트 실패 - 상품 없음: productId={}", productId);
            return createErrorResponse(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            log.error("상품 정보 업데이트 중 오류 발생: productId={}, error={}", productId, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "상품 정보 업데이트 중 오류가 발생했습니다.");
        }
    }

    /**
     * 상품 상태 업데이트 API
     * 
     * @param productId 상품 ID
     * @param isSellingAvailable 판매 가능 여부
     * @return 업데이트 결과
     */
    @PutMapping("/status/{productId}")
    public ResponseEntity<Object> updateProductStatus(
            @PathVariable("productId") Long productId,
            @RequestParam("isSellingAvailable") boolean isSellingAvailable) {
        log.info("상품 상태 업데이트 요청: productId={}, isSellingAvailable={}", productId, isSellingAvailable);
        
        try {
            sellProductService.updateStatus(productId, isSellingAvailable);
            log.info("상품 상태 업데이트 완료: productId={}, isSellingAvailable={}", productId, isSellingAvailable);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("상품 상태 업데이트 실패 - 상품 없음: productId={}", productId);
            return createErrorResponse(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            log.error("상품 상태 업데이트 중 오류 발생: productId={}, error={}", productId, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "상품 상태 업데이트 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 상품 상태 상세 업데이트 API
     * 
     * @param productId 상품 ID
     * @param status 상품 상태 (AVAILABLE, UNAVAILABLE, RESERVED, SOLD_OUT)
     * @return 업데이트 결과
     */
    @PutMapping("/status/detail/{productId}")
    public ResponseEntity<Object> updateDetailProductStatus(
            @PathVariable("productId") Long productId,
            @RequestParam("status") String status) {
        log.info("상품 상태 상세 업데이트 요청: productId={}, status={}", productId, status);
        
        try {
            sellProductService.updateProductStatus(productId, status);
            log.info("상품 상태 상세 업데이트 완료: productId={}, status={}", productId, status);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            String errorMessage = e.getMessage();
            log.warn("상품 상태 상세 업데이트 실패: productId={}, error={}", productId, errorMessage);
            
            if (errorMessage.contains("상품을 찾을 수 없습니다")) {
                return createErrorResponse(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", errorMessage);
            } else {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_STATUS", errorMessage);
            }
        } catch (Exception e) {
            log.error("상품 상태 상세 업데이트 중 오류 발생: productId={}, error={}", productId, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "상품 상태 업데이트 중 오류가 발생했습니다.");
        }
    }

    /**
     * 노출 가능 상품 목록 조회 API
     * 
     * @return 노출 가능한 상품 목록
     */
    @GetMapping("/visible")
    public ResponseEntity<Object> getVisibleProducts() {
        log.info("노출 가능 상품 목록 조회 요청");
        
        try {
            List<SellProductResponseDto> visibleProducts = sellProductService.getVisibleProducts();
            log.info("노출 가능 상품 목록 조회 완료: count={}", visibleProducts.size());
            return ResponseEntity.ok(visibleProducts);
        } catch (Exception e) {
            log.error("노출 가능 상품 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "상품 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 노출 가능 상품 목록 페이징 조회 API
     * 
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 페이징된 노출 가능한 상품 목록
     */
    @GetMapping("/visible/paged")
    public ResponseEntity<Object> getVisibleProductsPaged(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("노출 가능 상품 목록 페이징 조회 요청: page={}, size={}", page, size);
        
        try {
            Page<SellProductResponseDto> visibleProducts = sellProductService.getVisibleProductsPaged(page, size);
            log.info("노출 가능 상품 목록 페이징 조회 완료: totalElements={}, totalPages={}",
                    visibleProducts.getTotalElements(), visibleProducts.getTotalPages());
            return ResponseEntity.ok(visibleProducts);
        } catch (Exception e) {
            log.error("노출 가능 상품 목록 페이징 조회 중 오류 발생: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "상품 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 카테고리별 상품 조회 API
     * 
     * @param category 상품 카테고리
     * @return 해당 카테고리의 상품 목록
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Object> getProductsByCategory(@PathVariable("category") String category) {
        log.info("카테고리별 상품 조회 요청: category={}", category);
        
        try {
            List<SellProductResponseDto> products = sellProductService.getProductsByCategory(category);
            log.info("카테고리별 상품 조회 완료: category={}, count={}", category, products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("카테고리별 상품 조회 중 오류 발생: category={}, error={}", category, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "카테고리별 상품 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 판매자별 상품 조회 API
     * 
     * @param sellerUserIdx 판매자 ID
     * @return 해당 판매자의 상품 목록
     */
    @GetMapping("/seller/{sellerUserIdx}")
    public ResponseEntity<Object> getProductsBySellerUserIdx(@PathVariable("sellerUserIdx") Long sellerUserIdx) {
        log.info("판매자별 상품 조회 요청: sellerUserIdx={}", sellerUserIdx);
        
        try {
            List<SellProductResponseDto> products = sellProductService.getProductsBySellerUserIdx(sellerUserIdx);
            log.info("판매자별 상품 조회 완료: sellerUserIdx={}, count={}", sellerUserIdx, products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("판매자별 상품 조회 중 오류 발생: sellerUserIdx={}, error={}", sellerUserIdx, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "판매자별 상품 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 추천 상품 조회 API
     * 
     * @return 추천 상품 목록
     */
    @GetMapping("/featured")
    public ResponseEntity<Object> getFeaturedProducts() {
        log.info("추천 상품 조회 요청");
        
        try {
            List<SellProductResponseDto> products = sellProductService.getFeaturedProducts();
            log.info("추천 상품 조회 완료: count={}", products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("추천 상품 조회 중 오류 발생: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "추천 상품 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 태그별 상품 조회 API
     * 
     * @param tag 상품 태그
     * @return 해당 태그의 상품 목록
     */
    @GetMapping("/tag/{tag}")
    public ResponseEntity<Object> getProductsByTag(@PathVariable("tag") String tag) {
        log.info("태그별 상품 조회 요청: tag={}", tag);
        
        try {
            List<SellProductResponseDto> products = sellProductService.getProductsByTag(tag);
            log.info("태그별 상품 조회 완료: tag={}, count={}", tag, products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("태그별 상품 조회 중 오류 발생: tag={}, error={}", tag, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "태그별 상품 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 가격 범위로 상품 조회 API
     * 
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @return 가격 범위 내의 상품 목록
     */
    @GetMapping("/price-range")
    public ResponseEntity<Object> getProductsByPriceRange(
            @RequestParam("minPrice") Integer minPrice,
            @RequestParam("maxPrice") Integer maxPrice) {
        log.info("가격 범위별 상품 조회 요청: minPrice={}, maxPrice={}", minPrice, maxPrice);
        
        try {
            if (minPrice > maxPrice) {
                log.warn("가격 범위 오류: 최소 가격이 최대 가격보다 큼: minPrice={}, maxPrice={}", minPrice, maxPrice);
                return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_PRICE_RANGE", "최소 가격은 최대 가격보다 작아야 합니다.");
            }
            
            List<SellProductResponseDto> products = sellProductService.getProductsByPriceRange(minPrice, maxPrice);
            log.info("가격 범위별 상품 조회 완료: minPrice={}, maxPrice={}, count={}", minPrice, maxPrice, products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("가격 범위별 상품 조회 중 오류 발생: minPrice={}, maxPrice={}, error={}", 
                    minPrice, maxPrice, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "가격 범위별 상품 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 인기 상품 조회 API (조회수 기준)
     * 
     * @return 인기 상품 목록
     */
    @GetMapping("/popular")
    public ResponseEntity<Object> getTopViewedProducts() {
        log.info("인기 상품 조회 요청");
        
        try {
            List<SellProductResponseDto> products = sellProductService.getTopViewedProducts();
            log.info("인기 상품 조회 완료: count={}", products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("인기 상품 조회 중 오류 발생: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "인기 상품 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 추천 상품 토글 API (추천 설정/해제)
     * 
     * @param productId 상품 ID
     * @return 업데이트 결과
     */
    @PutMapping("/featured/toggle/{productId}")
    public ResponseEntity<Object> toggleFeaturedStatus(@PathVariable("productId") Long productId) {
        log.info("추천 상품 토글 요청: productId={}", productId);
        
        try {
            sellProductService.toggleFeaturedStatus(productId);
            log.info("추천 상품 토글 완료: productId={}", productId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("추천 상품 토글 실패 - 상품 없음: productId={}", productId);
            return createErrorResponse(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            log.error("추천 상품 토글 중 오류 발생: productId={}, error={}", productId, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "추천 상품 설정 중 오류가 발생했습니다.");
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
}
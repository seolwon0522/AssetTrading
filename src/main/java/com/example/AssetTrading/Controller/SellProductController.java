package com.example.AssetTrading.Controller;

import com.example.AssetTrading.Dto.SellProductRequestDto;
import com.example.AssetTrading.Dto.SellProductResponseDto;
import com.example.AssetTrading.Service.SellProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // 상품 상태 업데이트  (기능 구현 완료)
    @PutMapping("/status/{productId}")
    public ResponseEntity<Void> updateProductStatus(@PathVariable Long productId,
                                                    @RequestParam boolean isSellingAvailable) {
        sellProductService.updateStatus(productId, isSellingAvailable);
        return ResponseEntity.ok().build();
    }

    // 노출 가능 상품 목록 조회( 기능 구현 완료)
    @GetMapping("/visible")
    public ResponseEntity<List<SellProductResponseDto>> getVisibleProducts() {
        List<SellProductResponseDto> visibleProducts = sellProductService.getVisibleProducts();
        return ResponseEntity.ok(visibleProducts);
    }
}
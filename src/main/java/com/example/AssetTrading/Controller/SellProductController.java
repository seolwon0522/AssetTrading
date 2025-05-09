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



    // 새 판매 상품 등록
    @PostMapping("/register/session")
    public ResponseEntity<SellProductResponseDto> createSellProduct(@RequestBody SellProductRequestDto requestDto,
                                                                HttpSession session) {
        SellProductResponseDto responseDto = sellProductService.createSellProductWithSession(requestDto, session);
        return ResponseEntity.ok(responseDto);
    }

    // 키워드로 상품 목록을 조회
    @GetMapping("/search")
    public ResponseEntity<List<SellProductResponseDto>> getAllSellProducts(@RequestParam String keyword) {
        List<SellProductResponseDto> products = sellProductService.getAllSellProducts(keyword);
        return ResponseEntity.ok(products);
    }

    // ID로 특정 판매 상vna정보를 조회
    @GetMapping("/seller/{sellerUserId}")
    public ResponseEntity<SellProductResponseDto> getSellProductBySellerId(@PathVariable Long sellerUserId) {
        SellProductResponseDto responseDto = sellProductService.getSellProductById(sellerUserId);
        return ResponseEntity.ok(responseDto);
    }

    // 특정 판매 상품을 삭제
    @DeleteMapping("/seller/{sellerUserId}")
    public ResponseEntity<Void> deleteSellProduct(@PathVariable Long sellerUserId) {
        sellProductService.deleteSellProduct(sellerUserId);
        return ResponseEntity.noContent().build();
    }

    // 상품 상태 업데이트 이건 그냥 추가해봄
    @PutMapping("/status/{productId}")
    public ResponseEntity<Void> updateProductStatus(@PathVariable Long productId,
                                                    @RequestParam boolean isSellingAvailable) {
        sellProductService.updateStatus(productId, isSellingAvailable);
        return ResponseEntity.ok().build();
    }

    // 노출 가능 상품 목록 조회
    @GetMapping("/visible")
    public ResponseEntity<List<SellProductResponseDto>> getVisibleProducts() {
        List<SellProductResponseDto> visibleProducts = sellProductService.getVisibleProducts();
        return ResponseEntity.ok(visibleProducts);
    }
}
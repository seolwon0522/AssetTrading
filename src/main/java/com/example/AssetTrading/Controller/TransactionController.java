package com.example.AssetTrading.Controller;

import com.example.AssetTrading.Dto.TransactionRequestDto;
import com.example.AssetTrading.Dto.TransactionResponseDto;
import com.example.AssetTrading.Entity.TransactionStatus;
import com.example.AssetTrading.Service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // CORS 활성화
public class TransactionController {

    private final TransactionService transactionService;

    // 1. 거래 요청
    @PostMapping("/request")
    public ResponseEntity<TransactionResponseDto> requestTransaction(@RequestBody TransactionRequestDto transactionDto) {
        return ResponseEntity.ok(transactionService.requestTransaction(transactionDto));
    }

    // 2. 거래 승인
    @PutMapping("/{transaction_idx}/process")
    public ResponseEntity<TransactionResponseDto> processTransaction(@PathVariable("transaction_idx") Long id) {
        return ResponseEntity.ok(transactionService.processTransaction(id));
    }

    // 3. 거래 완료
    @PutMapping("/{transaction_idx}/complete")
    public ResponseEntity<TransactionResponseDto> completeTransaction(@PathVariable("transaction_idx") Long id) {
        return ResponseEntity.ok(transactionService.completeTransaction(id));
    }

    // 4. 거래 취소
    @PutMapping("/{transaction_idx}/cancel")
    public ResponseEntity<TransactionResponseDto> cancelTransaction(@PathVariable("transaction_idx") Long id) {
        return ResponseEntity.ok(transactionService.cancelTransaction(id));
    }
    
    // 5. 거래 상세 조회
    @GetMapping("/{transaction_idx}")
    public ResponseEntity<TransactionResponseDto> getTransaction(@PathVariable("transaction_idx") Long id) {
        return ResponseEntity.ok(transactionService.getTransaction(id));
    }
    
    // 6. 구매자의 거래 내역 조회
    @GetMapping("/buyer/{buyer_id}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByBuyer(@PathVariable("buyer_id") Long buyerId) {
        return ResponseEntity.ok(transactionService.getTransactionsByBuyer(buyerId));
    }
    
    // 7. 판매자의 거래 내역 조회
    @GetMapping("/seller/{seller_id}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsBySeller(@PathVariable("seller_id") Long sellerId) {
        return ResponseEntity.ok(transactionService.getTransactionsBySeller(sellerId));
    }
    
    // 8. 상품의 거래 내역 조회
    @GetMapping("/product/{product_id}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByProduct(@PathVariable("product_id") Long productId) {
        return ResponseEntity.ok(transactionService.getTransactionsByProduct(productId));
    }
    
    // 9. 거래 상태별 조회
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByStatus(
            @PathVariable("status") String status) {
        try {
            TransactionStatus transactionStatus = TransactionStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(transactionService.getTransactionsByStatus(transactionStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // 10. 모든 거래 내역 조회 (관리자용)
    @GetMapping("/all")
    public ResponseEntity<List<TransactionResponseDto>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }
}

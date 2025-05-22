package com.example.AssetTrading.Controller;

import com.example.AssetTrading.Dto.TransactionRequestDto;
import com.example.AssetTrading.Dto.TransactionResponseDto;
import com.example.AssetTrading.Entity.TransactionStatus;
import com.example.AssetTrading.Exception.ResourceNotFoundException;
import com.example.AssetTrading.Service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // CORS 활성화
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 거래 요청
     * 
     * @param transactionDto 거래 요청 정보
     * @return 생성된 거래 정보
     */
    @PostMapping("/request")
    public ResponseEntity<Object> requestTransaction(@RequestBody @Validated TransactionRequestDto transactionDto) {
        log.info("거래 요청: buyerId={}, productId={}", transactionDto.getBuyerId(), transactionDto.getProductId());
        
        try {
            TransactionResponseDto responseDto = transactionService.requestTransaction(transactionDto);
            log.info("거래 요청 성공: transactionId={}", responseDto.getTransaction_idx());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (ResourceNotFoundException e) {
            log.warn("거래 요청 실패 - 리소스 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("거래 요청 실패 - 상태 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("거래 요청 실패 - 유효하지 않은 입력: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("거래 요청 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "거래 요청 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 거래 승인
     * 
     * @param id 거래 ID
     * @return 업데이트된 거래 정보
     */
    @PutMapping("/{transactionId}/process")
    public ResponseEntity<Object> processTransaction(@PathVariable("transactionId") Long id) {
        log.info("거래 승인 요청: transactionId={}", id);
        
        try {
            TransactionResponseDto responseDto = transactionService.processTransaction(id);
            log.info("거래 승인 완료: transactionId={}, status={}", id, responseDto.getStatus());
            return ResponseEntity.ok(responseDto);
        } catch (ResourceNotFoundException e) {
            log.warn("거래 승인 실패 - 리소스 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("거래 승인 실패 - 상태 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("거래 승인 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "거래 승인 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 거래 완료
     * 
     * @param id 거래 ID
     * @return 업데이트된 거래 정보
     */
    @PutMapping("/{transactionId}/complete")
    public ResponseEntity<Object> completeTransaction(@PathVariable("transactionId") Long id) {
        log.info("거래 완료 요청: transactionId={}", id);
        
        try {
            TransactionResponseDto responseDto = transactionService.completeTransaction(id);
            log.info("거래 완료 처리: transactionId={}, status={}", id, responseDto.getStatus());
            return ResponseEntity.ok(responseDto);
        } catch (ResourceNotFoundException e) {
            log.warn("거래 완료 실패 - 리소스 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("거래 완료 실패 - 상태 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("거래 완료 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "거래 완료 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 거래 취소
     * 
     * @param id 거래 ID
     * @return 업데이트된 거래 정보
     */
    @PutMapping("/{transactionId}/cancel")
    public ResponseEntity<Object> cancelTransaction(@PathVariable("transactionId") Long id) {
        log.info("거래 취소 요청: transactionId={}", id);
        
        try {
            TransactionResponseDto responseDto = transactionService.cancelTransaction(id);
            log.info("거래 취소 완료: transactionId={}, status={}", id, responseDto.getStatus());
            return ResponseEntity.ok(responseDto);
        } catch (ResourceNotFoundException e) {
            log.warn("거래 취소 실패 - 리소스 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("거래 취소 실패 - 상태 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("거래 취소 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "거래 취소 처리 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 거래 상세 조회
     * 
     * @param id 거래 ID
     * @return 거래 정보
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<Object> getTransaction(@PathVariable("transactionId") Long id) {
        log.info("거래 상세 조회 요청: transactionId={}", id);
        
        try {
            TransactionResponseDto responseDto = transactionService.getTransaction(id);
            return ResponseEntity.ok(responseDto);
        } catch (ResourceNotFoundException e) {
            log.warn("거래 조회 실패 - 리소스 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("거래 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "거래 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 구매자의 거래 내역 조회
     * 
     * @param buyerId 구매자 ID
     * @return 거래 목록
     */
    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<Object> getTransactionsByBuyer(@PathVariable("buyerId") Long buyerId) {
        log.info("구매자 거래 내역 조회: buyerId={}", buyerId);
        
        try {
            List<TransactionResponseDto> transactions = transactionService.getTransactionsByBuyer(buyerId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("구매자 거래 내역 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "구매자 거래 내역 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 판매자의 거래 내역 조회
     * 
     * @param sellerId 판매자 ID
     * @return 거래 목록
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Object> getTransactionsBySeller(@PathVariable("sellerId") Long sellerId) {
        log.info("판매자 거래 내역 조회: sellerId={}", sellerId);
        
        try {
            List<TransactionResponseDto> transactions = transactionService.getTransactionsBySeller(sellerId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("판매자 거래 내역 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "판매자 거래 내역 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 상품별 거래 내역 조회
     * 
     * @param productId 상품 ID
     * @return 거래 목록
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Object> getTransactionsByProduct(@PathVariable("productId") Long productId) {
        log.info("상품 거래 내역 조회: productId={}", productId);
        
        try {
            List<TransactionResponseDto> transactions = transactionService.getTransactionsByProduct(productId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("상품 거래 내역 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "상품 거래 내역 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 거래 상태별 조회
     * 
     * @param status 거래 상태
     * @return 거래 목록
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Object> getTransactionsByStatus(@PathVariable("status") String status) {
        log.info("거래 상태별 조회: status={}", status);
        
        try {
            TransactionStatus transactionStatus = TransactionStatus.valueOf(status.toUpperCase());
            List<TransactionResponseDto> transactions = transactionService.getTransactionsByStatus(transactionStatus);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            log.warn("거래 상태별 조회 실패 - 유효하지 않은 상태: {}", status);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "유효하지 않은 거래 상태입니다: " + status));
        } catch (Exception e) {
            log.error("거래 상태별 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "거래 상태별 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 모든 거래 내역 조회 (관리자용)
     * 
     * @return 모든 거래 목록
     */
    @GetMapping("/all")
    public ResponseEntity<Object> getAllTransactions() {
        log.info("전체 거래 내역 조회 요청");
        
        try {
            List<TransactionResponseDto> transactions = transactionService.getAllTransactions();
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("전체 거래 내역 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "전체 거래 내역 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 전역 예외 처리
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("리소스를 찾을 수 없음: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException e) {
        log.warn("거래 상태 오류: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("유효하지 않은 입력값: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }
}

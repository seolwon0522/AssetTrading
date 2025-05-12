package com.example.AssetTrading.Controller;

import com.example.AssetTrading.Dto.TransactionRequestDto;
import com.example.AssetTrading.Dto.TransactionResponseDto;
import com.example.AssetTrading.Service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
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

    // 거래 취소
    @PutMapping("/{transaction_idx}/cancel")
    public ResponseEntity<TransactionResponseDto> cancelTransaction(@PathVariable("transaction_idx") Long id) {
        return ResponseEntity.ok(transactionService.cancelTransaction(id));
    }
}

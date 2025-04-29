package com.example.AssetTrading.Service;

import com.example.AssetTrading.Entity.SellProduct;
import com.example.AssetTrading.Entity.Transaction;
import com.example.AssetTrading.Entity.TransactionStatus;
import com.example.AssetTrading.Entity.User;
import com.example.AssetTrading.Repository.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@AllArgsConstructor
public class TransactionService {

    private TransactionRepository transactionRepository;

    // 거래 생성
    public Transaction createTransaction(User buyer, User seller, SellProduct product){
        Transaction transaction = Transaction.builder()
                .buyer(buyer)
                .seller(seller)
                .sellProduct(product)
                .status(TransactionStatus.REQUESTED) // 첫 등록이라 REQUESTED
                .build();

        return transactionRepository.save(transaction);
    }

    // 거래 시작
    public Transaction startTransaction(Long transactionId){
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("찾을 수 없는 거래"));
        return null; // 임시
    }
}

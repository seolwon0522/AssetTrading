package com.example.AssetTrading.Repository;

import com.example.AssetTrading.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByBuyer(User buyer);
    List<Transaction> findBySeller(User seller);
    List<Transaction> findByStatus(TransactionStatus status);
    List<Transaction> findBySellProduct(SellProduct sellProduct);
    
    // Find transactions by product and status
    List<Transaction> findBySellProductAndStatus(SellProduct sellProduct, TransactionStatus status);
    
    // Check if a transaction exists for a buyer, product, and status
    boolean existsByBuyerAndSellProductAndStatus(User buyer, SellProduct sellProduct, TransactionStatus status);
    
    // Find transactions by product, status, and exclude a specific transaction
    List<Transaction> findBySellProductAndStatusAndTransactionIdxNot(
            SellProduct sellProduct, 
            TransactionStatus status, 
            Long transactionIdx);
}

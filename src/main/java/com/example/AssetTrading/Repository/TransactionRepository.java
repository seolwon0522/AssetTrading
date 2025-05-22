package com.example.AssetTrading.Repository;

import com.example.AssetTrading.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    // 구매자별, 생성 시간 범위별 조회
    List<Transaction> findByBuyerAndCreatedTimeBetween(User buyer, LocalDateTime start, LocalDateTime end);
    
    // 판매자별, 생성 시간 범위별 조회
    List<Transaction> findBySellerAndCreatedTimeBetween(User seller, LocalDateTime start, LocalDateTime end);
    
    // 상태별 거래 수 집계
    long countByStatus(TransactionStatus status);
    
    // 특정 시간 이후 생성된 거래 수 집계
    long countByCreatedTimeAfter(LocalDateTime dateTime);
    
    // 특정 시간 범위 내 생성된 거래 수 집계
    long countByCreatedTimeBetween(LocalDateTime start, LocalDateTime end);
    
    // 특정 기간별 거래량 통계
    @Query("SELECT DATE(t.createdTime) as date, COUNT(t) as count FROM Transaction t " +
           "WHERE t.createdTime BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(t.createdTime) ORDER BY date")
    List<Object[]> getTransactionCountsByDateRange(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    // 사용자별 거래 금액 합계 (구매자)
    @Query("SELECT SUM(p.productPrice) FROM Transaction t JOIN t.sellProduct p " +
           "WHERE t.buyer = :user AND t.status = :status")
    Integer calculateTotalBuyAmount(@Param("user") User user, @Param("status") TransactionStatus status);
    
    // 사용자별 거래 금액 합계 (판매자)
    @Query("SELECT SUM(p.productPrice) FROM Transaction t JOIN t.sellProduct p " +
           "WHERE t.seller = :user AND t.status = :status")
    Integer calculateTotalSellAmount(@Param("user") User user, @Param("status") TransactionStatus status);
}

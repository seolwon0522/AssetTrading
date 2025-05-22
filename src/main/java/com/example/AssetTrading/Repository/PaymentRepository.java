package com.example.AssetTrading.Repository;

import com.example.AssetTrading.Entity.Payment;
import com.example.AssetTrading.Entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * 거래별 결제 정보 조회
     */
    Optional<Payment> findByTransaction(Transaction transaction);
    
    /**
     * 거래별 결제 정보 존재 여부 확인
     */
    boolean existsByTransaction(Transaction transaction);
    
    /**
     * 구매자별 결제 내역 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.transaction.buyer.user_idx = :buyerUserIdx")
    List<Payment> findByTransactionBuyerUser_idx(@Param("buyerUserIdx") Long buyerUserIdx);
    
    /**
     * 판매자별 결제 내역 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.transaction.seller.user_idx = :sellerUserIdx")
    List<Payment> findByTransactionSellerUser_idx(@Param("sellerUserIdx") Long sellerUserIdx);
    
    /**
     * 결제 상태별 조회
     */
    List<Payment> findByPaymentStatus(String paymentStatus);
} 
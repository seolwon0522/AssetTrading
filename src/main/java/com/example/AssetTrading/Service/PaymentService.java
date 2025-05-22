package com.example.AssetTrading.Service;

import com.example.AssetTrading.Entity.Payment;
import com.example.AssetTrading.Entity.Transaction;
import com.example.AssetTrading.Entity.TransactionStatus;
import com.example.AssetTrading.Repository.PaymentRepository;
import com.example.AssetTrading.Repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    /**
     * 결제 정보 생성
     * 
     * @param transactionId 거래 ID
     * @param amount 결제 금액
     * @param paymentMethod 결제 방법
     * @return 생성된 결제 정보
     */
    public Payment createPayment(Long transactionId, Integer amount, String paymentMethod) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("해당 거래가 존재하지 않습니다. transaction_idx: " + transactionId));
        
        // 이미 결제가 있는지 확인
        if (paymentRepository.existsByTransaction(transaction)) {
            throw new IllegalStateException("이미 결제 정보가 존재합니다.");
        }
        
        // 결제 키 생성 (실제 환경에서는 외부 결제 시스템에서 제공됨)
        String paymentKey = UUID.randomUUID().toString();
        
        Payment payment = Payment.builder()
            .transaction(transaction)
            .amount(amount)
            .paymentMethod(paymentMethod)
            .paymentStatus("PENDING")
            .paymentKey(paymentKey)
            .createdAt(LocalDateTime.now())
            .build();
        
        return paymentRepository.save(payment);
    }
    
    /**
     * 결제 승인
     * 
     * @param paymentId 결제 ID
     * @return 승인된 결제 정보
     */
    public Payment approvePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("해당 결제 정보가 존재하지 않습니다. payment_idx: " + paymentId));
        
        if (!"PENDING".equals(payment.getPaymentStatus())) {
            throw new IllegalStateException("승인 가능한 상태가 아닙니다: " + payment.getPaymentStatus());
        }
        
        // 결제 상태 업데이트
        payment.setPaymentStatus("COMPLETED");
        payment.setApprovedAt(LocalDateTime.now());
        
        // 거래 상태 업데이트
        Transaction transaction = payment.getTransaction();
        transaction.setStatus(TransactionStatus.PROCESSING);
        transaction.setProcessedTime(LocalDateTime.now());
        
        // 알림 전송
        notificationService.createTransactionNotification(
            transaction.getBuyer().getUser_idx(),
            transaction.getTransactionIdx(),
            "결제가 완료되었습니다. 판매자가 배송을 준비하고 있습니다."
        );
        
        notificationService.createTransactionNotification(
            transaction.getSeller().getUser_idx(),
            transaction.getTransactionIdx(),
            "구매자가 결제를 완료했습니다. 상품을 준비해주세요."
        );
        
        return paymentRepository.save(payment);
    }
    
    /**
     * 결제 취소 및 환불
     * 
     * @param paymentId 결제 ID
     * @param reason 환불 사유
     * @return 환불된 결제 정보
     */
    public Payment refundPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("해당 결제 정보가 존재하지 않습니다. payment_idx: " + paymentId));
        
        if (!"COMPLETED".equals(payment.getPaymentStatus())) {
            throw new IllegalStateException("환불 가능한 상태가 아닙니다: " + payment.getPaymentStatus());
        }
        
        // 결제 상태 업데이트
        payment.setPaymentStatus("REFUNDED");
        payment.setRefundReason(reason);
        payment.setRefundedAt(LocalDateTime.now());
        
        // 거래 상태 업데이트
        Transaction transaction = payment.getTransaction();
        transaction.setStatus(TransactionStatus.CANCELED);
        transaction.setUpdatedTime(LocalDateTime.now());
        
        // 알림 전송
        notificationService.createTransactionNotification(
            transaction.getBuyer().getUser_idx(),
            transaction.getTransactionIdx(),
            "결제가 환불되었습니다. 사유: " + reason
        );
        
        notificationService.createTransactionNotification(
            transaction.getSeller().getUser_idx(),
            transaction.getTransactionIdx(),
            "거래가 취소되고 결제가 환불되었습니다. 사유: " + reason
        );
        
        return paymentRepository.save(payment);
    }
    
    /**
     * 결제 정보 조회
     * 
     * @param paymentId 결제 ID
     * @return 결제 정보
     */
    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("해당 결제 정보가 존재하지 않습니다. payment_idx: " + paymentId));
    }
    
    /**
     * 거래별 결제 정보 조회
     * 
     * @param transactionId 거래 ID
     * @return 결제 정보
     */
    public Payment getPaymentByTransactionId(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("해당 거래가 존재하지 않습니다. transaction_idx: " + transactionId));
        
        return paymentRepository.findByTransaction(transaction)
            .orElseThrow(() -> new IllegalArgumentException("해당 거래의 결제 정보가 존재하지 않습니다."));
    }
    
    /**
     * 사용자별 결제 내역 조회
     * 
     * @param userId 사용자 ID
     * @param isBuyer 구매자 여부 (true: 구매, false: 판매)
     * @return 결제 내역 목록
     */
    public List<Payment> getPaymentsByUserId(Long userId, boolean isBuyer) {
        if (isBuyer) {
            return paymentRepository.findByTransactionBuyerUser_idx(userId);
        } else {
            return paymentRepository.findByTransactionSellerUser_idx(userId);
        }
    }
} 
package com.example.AssetTrading.Service;

import com.example.AssetTrading.Dto.TransactionRequestDto;
import com.example.AssetTrading.Dto.TransactionResponseDto;
import com.example.AssetTrading.Entity.*;
import com.example.AssetTrading.Exception.TransactionException;
import com.example.AssetTrading.Repository.SellProductRepository;
import com.example.AssetTrading.Repository.TransactionRepository;
import com.example.AssetTrading.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final SellProductRepository sellProductRepository;

    // 거래 요청
    @Transactional
    public TransactionResponseDto requestTransaction(TransactionRequestDto requestDto) {
        User buyer = userRepository.findById(requestDto.getBuyerId())
                .orElseThrow(() -> new TransactionException("존재하지 않는 구매자입니다.", "USER_NOT_FOUND"));

        SellProduct product = sellProductRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new TransactionException("존재하지 않는 상품입니다.", "PRODUCT_NOT_FOUND"));

        // 판매자와 구매자가 같은 경우 예외 처리
        if (product.getSellerUserIdx().equals(buyer.getUser_idx())) {
            throw new TransactionException("자신의 상품은 구매할 수 없습니다.", "SELF_PURCHASE_NOT_ALLOWED");
        }

        // 상품 상태 확인
        if (product.getProductStatus() != ProductStatus.AVAILABLE) {
            throw new TransactionException("이미 거래중이거나 완료된 상품입니다.", "PRODUCT_NOT_AVAILABLE");
        }

        User seller = userRepository.findById(product.getSellerUserIdx())
                .orElseThrow(() -> new TransactionException("상품에 판매자 정보가 없습니다.", "SELLER_NOT_FOUND"));

        // 기존 거래 요청 확인 (동일한 구매자와 상품에 대한 중복 요청 방지)
        if (transactionRepository.existsByBuyerAndSellProductAndStatus(buyer, product, TransactionStatus.REQUESTED)) {
            throw new TransactionException("이미 해당 상품에 대한 거래 요청이 존재합니다.", "DUPLICATE_REQUEST");
        }

        Transaction transaction = Transaction.builder()
                .buyer(buyer)
                .seller(seller)
                .sellProduct(product)
                .status(TransactionStatus.REQUESTED)
                .createdTime(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        return TransactionResponseDto.fromEntity(savedTransaction);
    }

    // 거래 승인
    @Transactional
    public TransactionResponseDto processTransaction(Long transaction_idx) {
        Transaction transaction = getTransactionById(transaction_idx);

        // 거래 상태 확인
        if (transaction.getStatus() != TransactionStatus.REQUESTED) {
            throw new TransactionException("이미 처리된 거래입니다.", "TRANSACTION_ALREADY_PROCESSED");
        }

        SellProduct product = transaction.getSellProduct();
        // 상품 상태 확인
        if (product.getProductStatus() != ProductStatus.AVAILABLE) {
            throw new TransactionException("이미 거래 중이거나 완료된 상품입니다.", "PRODUCT_NOT_AVAILABLE");
        }

        // 다른 거래 요청들 자동 취소 (동일 상품에 대한 다른 요청)
        cancelOtherRequests(transaction);

        transaction.setStatus(TransactionStatus.PROCESSING);
        product.setProductStatus(ProductStatus.RESERVED);
        transaction.setProcessedTime(LocalDateTime.now());

        return TransactionResponseDto.fromEntity(transaction);
    }

    // 거래 완료
    @Transactional
    public TransactionResponseDto completeTransaction(Long transaction_idx) {
        Transaction transaction = getTransactionById(transaction_idx);

        // 거래 상태 확인
        if (transaction.getStatus() != TransactionStatus.PROCESSING) {
            throw new TransactionException("진행 중인 거래만 완료할 수 있습니다.", "INVALID_TRANSACTION_STATUS");
        }

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setSuccessTime(LocalDateTime.now());

        SellProduct product = transaction.getSellProduct();
        product.setProductStatus(ProductStatus.SOLD_OUT);

        return TransactionResponseDto.fromEntity(transaction);
    }

    // 거래 취소
    @Transactional
    public TransactionResponseDto cancelTransaction(Long transaction_idx) {
        Transaction transaction = getTransactionById(transaction_idx);

        // 완료된 거래는 취소 불가
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            throw new TransactionException("완료된 거래는 취소할 수 없습니다.", "COMPLETED_TRANSACTION_CANNOT_CANCEL");
        }

        transaction.setStatus(TransactionStatus.CANCELED);
        transaction.setDeletedTime(LocalDateTime.now());

        // 거래 중이었던 상품은 다시 판매 가능 상태로 변경
        if (transaction.getStatus() == TransactionStatus.PROCESSING) {
            transaction.getSellProduct().setProductStatus(ProductStatus.AVAILABLE);
        }

        return TransactionResponseDto.fromEntity(transaction);
    }
    
    // 거래 상세 조회
    public TransactionResponseDto getTransaction(Long transaction_idx) {
        return TransactionResponseDto.fromEntity(getTransactionById(transaction_idx));
    }
    
    // 구매자별 거래 내역 조회
    public List<TransactionResponseDto> getTransactionsByBuyer(Long buyerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new TransactionException("존재하지 않는 사용자입니다.", "USER_NOT_FOUND"));
                
        return transactionRepository.findByBuyer(buyer).stream()
                .map(TransactionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 판매자별 거래 내역 조회
    public List<TransactionResponseDto> getTransactionsBySeller(Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new TransactionException("존재하지 않는 사용자입니다.", "USER_NOT_FOUND"));
                
        return transactionRepository.findBySeller(seller).stream()
                .map(TransactionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 상품별 거래 내역 조회
    public List<TransactionResponseDto> getTransactionsByProduct(Long productId) {
        SellProduct product = sellProductRepository.findById(productId)
                .orElseThrow(() -> new TransactionException("존재하지 않는 상품입니다.", "PRODUCT_NOT_FOUND"));
                
        return transactionRepository.findBySellProduct(product).stream()
                .map(TransactionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 거래 상태별 조회
    public List<TransactionResponseDto> getTransactionsByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status).stream()
                .map(TransactionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 모든 거래 내역 조회 (관리자용)
    public List<TransactionResponseDto> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(TransactionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 거래 ID로 거래 조회 (내부 사용 메서드)
    private Transaction getTransactionById(Long transaction_idx) {
        return transactionRepository.findById(transaction_idx)
                .orElseThrow(() -> new TransactionException("존재하지 않는 거래입니다.", "TRANSACTION_NOT_FOUND"));
    }
    
    @Transactional
    private void cancelOtherRequests(Transaction currentTransaction) {
        // 현재 거래의 상품에 대한 다른 요청들을 모두 취소 처리
        SellProduct product = currentTransaction.getSellProduct();
        
        transactionRepository.findBySellProductAndStatusAndTransactionIdxNot(
                product, 
                TransactionStatus.REQUESTED, 
                currentTransaction.getTransactionIdx()
            ).forEach(transaction -> {
                transaction.setStatus(TransactionStatus.CANCELED);
                transaction.setDeletedTime(LocalDateTime.now());
            });
    }
}

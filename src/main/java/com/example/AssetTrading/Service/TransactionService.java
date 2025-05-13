package com.example.AssetTrading.Service;

import com.example.AssetTrading.Dto.ChatRoomRequestDto;
import com.example.AssetTrading.Dto.TransactionRequestDto;
import com.example.AssetTrading.Dto.TransactionResponseDto;
import com.example.AssetTrading.Entity.*;
import com.example.AssetTrading.Exception.TransactionException;
import com.example.AssetTrading.Repository.ChatRoomRepository;
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
    private final ChatService chatService;
    private final ChatRoomRepository chatRoomRepository;

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
        
        // 거래 요청 시 채팅방 생성
        createChatRoomForTransaction(savedTransaction);

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
        
        // 거래 상태 변경 시 채팅방에 알림 메시지 전송
        sendTransactionStatusUpdateToChat(transaction, "승인");

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
        
        // 거래 상태 변경 시 채팅방에 알림 메시지 전송
        sendTransactionStatusUpdateToChat(transaction, "완료");

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
        
        // 거래 상태 변경 시 채팅방에 알림 메시지 전송
        sendTransactionStatusUpdateToChat(transaction, "취소");

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
    
    // 동일 상품에 대한 다른 거래 요청 자동 취소
    @Transactional
    private void cancelOtherRequests(Transaction currentTransaction) {
        SellProduct product = currentTransaction.getSellProduct();
        List<Transaction> otherRequests = transactionRepository.findBySellProductAndStatusAndTransactionIdxNot(
                product, 
                TransactionStatus.REQUESTED, 
                currentTransaction.getTransactionIdx());
        
        for (Transaction otherRequest : otherRequests) {
            otherRequest.setStatus(TransactionStatus.CANCELED);
            otherRequest.setDeletedTime(LocalDateTime.now());
            
            // 취소된 거래에 대한 채팅방 알림 전송
            sendTransactionStatusUpdateToChat(otherRequest, "취소");
        }
    }
    
    // 거래에 대한 채팅방 생성
    private void createChatRoomForTransaction(Transaction transaction) {
        // 이미 채팅방이 존재하는지 확인
        if (chatRoomRepository.existsByTransactionIdx(transaction.getTransactionIdx().intValue())) {
            return;
        }
        
        User buyer = transaction.getBuyer();
        User seller = transaction.getSeller();
        SellProduct product = transaction.getSellProduct();
        
        ChatRoomRequestDto chatRoomRequestDto = ChatRoomRequestDto.builder()
                .transaction_idx(transaction.getTransactionIdx().intValue())
                .buyer_user_idx(buyer.getUser_idx())
                .buyer_user_id(buyer.getUserId())
                .buyerName(buyer.getUserName())
                .seller_user_idx(seller.getUser_idx())
                .seller_user_id(seller.getUserId())
                .sellerName(seller.getUserName())
                .product_idx(product.getId())
                .productTitle(product.getProductTitle())
                .build();
        
        chatService.createChatRoom(chatRoomRequestDto);
    }
    
    // 거래 상태 변경 시 채팅방에 알림 메시지 전송
    private void sendTransactionStatusUpdateToChat(Transaction transaction, String status) {
        try {
            chatService.sendTransactionStatusUpdate(transaction.getTransactionIdx().intValue(), status);
        } catch (Exception e) {
            // 채팅방이 없는 경우 등의 예외 처리 (로깅 등 추가 가능)
            // 채팅 알림 실패가 거래 처리를 방해하지 않도록 예외를 잡아서 처리
        }
    }
}

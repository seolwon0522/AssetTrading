package com.example.AssetTrading.Service;

import com.example.AssetTrading.Dto.TransactionRequestDto;
import com.example.AssetTrading.Dto.TransactionResponseDto;
import com.example.AssetTrading.Entity.*;
import com.example.AssetTrading.Repository.SellProductRepository;
import com.example.AssetTrading.Repository.TransactionRepository;
import com.example.AssetTrading.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final SellProductRepository sellProductRepository;
    /** 채팅방 **/

    // 예외들은 따로 생각하고 넣을라고 일단 몇개 넣어놓음. 예외는 무시하고 읽어보셈.
    // 거래 요청
    @Transactional
    public TransactionResponseDto requestTransaction(TransactionRequestDto requestDto){

        User buyer = userRepository.findById(requestDto.getBuyerId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구매자입니다."));

        SellProduct product = sellProductRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        // [예외처리] 그 거래의 상태가 판매중 인지 확인
        if (product.getProductStatus() != ProductStatus.AVAILABLE) {
            throw new IllegalStateException("이미 거래중이거나 완료된 상품입니다.");
        }

        User seller = product.getSellerUserIdx();
        if (seller == null) {
            throw new IllegalStateException("상품에 판매자 정보가 없습니다.");
        }

        // 2. TransactionRequest Entity를 안 해놨기 때문에
        Transaction transaction = Transaction.builder()
                .buyer(buyer)
                .seller(product.getSellerUserIdx()) // Seller는 product에서 땡겨옴
                .sellProduct(product)
                .status(TransactionStatus.REQUESTED) // 첫 등록이라 REQUESTED
                .createdTime(LocalDateTime.now())
                .build();

        return TransactionResponseDto.fromEntity(transactionRepository.save(transaction));
    }


    // 거래 승인, acceptTransaction으로 하면 보기 쉬울수도
    @Transactional
    public TransactionResponseDto processTransaction(Long transaction_idx) {

        // [예외처리] 1. 존재하는 거래인지 유무
        Transaction transaction = transactionRepository.findById(transaction_idx)
                .orElseThrow(() -> new IllegalArgumentException("거래 찾을 수 없음."));

        // [예외처리] 2. 그 거래가 처리된 거래인지
        if (transaction.getStatus() != TransactionStatus.REQUESTED) {
            throw new IllegalStateException("이미 처리된 거래입니다.");
        }

        SellProduct product = transaction.getSellProduct();
        // [예외처리] 3. 그 거래의 상태가 판매중 인지 확인
        if (product.getProductStatus() != ProductStatus.AVAILABLE) {
            throw new IllegalStateException("이미 거래 중이거나 완료된 상품입니다.");
        }

        transaction.setStatus(TransactionStatus.PROCESSING); // 거래 상태를 승인으로 바꿈
        product.setProductStatus(ProductStatus.RESERVED); // 예약중으로 바뀜. 상품 상태에 추가해도 좋을듯(거래중)
        transaction.setProcessedTime(LocalDateTime.now()); // 거래 승인 시간 체크

        return TransactionResponseDto.fromEntity(transaction);
    }


    // 거래 완료
    @Transactional
    public TransactionResponseDto completeTransaction(Long transaction_idx){

        // [예외처리] 1. 존재하는 거래인지 유무
        Transaction transaction = transactionRepository.findById(transaction_idx)
                .orElseThrow(() -> new IllegalArgumentException("거래 찾을 수 없음."));

        // [예외처리] 2. 현재 상태가 PROCESSING인지 확인
        if (transaction.getStatus() != TransactionStatus.PROCESSING) {
            throw new IllegalStateException("진행 중인 거래만 완료됨.");
        }

        transaction.setStatus(TransactionStatus.COMPLETED); // 거래 상태를 완료로 바꿈
        transaction.setSuccessTime(LocalDateTime.now()); // 거래 완료 시간 체크


        SellProduct product = transaction.getSellProduct(); // 상품의 상태를 바꾸기위해 참조
        product.setProductStatus(ProductStatus.SOLD_OUT); // 거래 완료이므로 상품을 SOLD_OUT 으로

        return TransactionResponseDto.fromEntity(transaction);
    }


    // 거래 취소
    @Transactional
    public TransactionResponseDto cancelTransaction(Long transaction_idx){

        // [예외처리] 1. 존재하는 거래인지 유무
        Transaction transaction = transactionRepository.findById(transaction_idx)
                .orElseThrow(() -> new IllegalArgumentException("거래 찾을 수 없음."));

        // [예외처리] 2. 완료된 거래는 취소 X
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            throw new IllegalStateException("완료된 거래는 취소할 수 없음.");
        }

        transaction.setStatus(TransactionStatus.CANCELED); // 거래 상태를 취소로 바꿈
        // 거래를 취소 한 시간 값 추가 예정

        return TransactionResponseDto.fromEntity(transaction);
    }
}

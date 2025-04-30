package com.example.AssetTrading.Entity;

public enum TransactionStatus {
    WAITTING, // 대기 상태 ( 상품을 올렸지만 아무 거래가 안 걸린 상태 ) 
    REQUESTED, // 거래 요청
    PROCESSING, // 거래 중
    COMPLETED, // 거래 성공
    CANCELED // 거래 실패
}


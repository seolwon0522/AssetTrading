package com.example.AssetTrading.Entity;

public enum TransactionStatus {
    REQUESTED,  // 거래 요청
    PROCESSING, // 거래 중
    COMPLETED,  // 거래 성공
    CANCELED    // 거래 실패
}


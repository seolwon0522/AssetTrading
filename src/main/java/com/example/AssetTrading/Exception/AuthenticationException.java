package com.example.AssetTrading.Exception;

/**
 * 인증 실패 시 발생하는 예외
 */
public class AuthenticationException extends RuntimeException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
} 
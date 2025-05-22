package com.example.AssetTrading.Repository;

import com.example.AssetTrading.Entity.Notification;
import com.example.AssetTrading.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 사용자별 알림 목록을 생성일자 내림차순으로 조회
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * 사용자별 읽지 않은 알림 목록 조회
     */
    List<Notification> findByUserAndIsReadFalse(User user);
    
    /**
     * 사용자별 읽지 않은 알림 개수 조회
     */
    long countByUserAndIsReadFalse(User user);
    
    /**
     * 알림 유형별 조회
     */
    List<Notification> findByUserAndNotificationType(User user, String notificationType);
} 
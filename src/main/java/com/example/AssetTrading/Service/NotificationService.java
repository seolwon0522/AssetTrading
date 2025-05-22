package com.example.AssetTrading.Service;

import com.example.AssetTrading.Entity.Notification;
import com.example.AssetTrading.Entity.User;
import com.example.AssetTrading.Repository.NotificationRepository;
import com.example.AssetTrading.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 새로운 알림을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param title 알림 제목
     * @param content 알림 내용
     * @param notificationType 알림 유형 (TRANSACTION, SYSTEM 등)
     * @param referenceId 참조 ID (거래 ID 등)
     * @return 생성된 알림
     */
    public Notification createNotification(Long userId, String title, String content, 
                                         String notificationType, Long referenceId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다. user_idx: " + userId));
        
        Notification notification = Notification.builder()
            .user(user)
            .title(title)
            .content(content)
            .notificationType(notificationType)
            .referenceId(referenceId)
            .isRead(false)
            .createdAt(LocalDateTime.now())
            .build();
        
        return notificationRepository.save(notification);
    }
    
    /**
     * 특정 사용자의 모든 알림을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자의 알림 목록
     */
    public List<Notification> getNotificationsByUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다. user_idx: " + userId));
        
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * 특정 사용자의 읽지 않은 알림 수를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 수
     */
    public long countUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다. user_idx: " + userId));
        
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
    
    /**
     * 알림을 읽음 상태로 변경합니다.
     * 
     * @param notificationId 알림 ID
     * @param userId 사용자 ID(권한 확인용)
     * @return 수정된 알림
     */
    public Notification markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("해당 알림이 존재하지 않습니다. notification_idx: " + notificationId));
        
        // 해당 사용자의 알림이 맞는지 확인
        if (!notification.getUser().getUser_idx().equals(userId)) {
            throw new IllegalArgumentException("해당 사용자의 알림이 아닙니다.");
        }
        
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }
    
    /**
     * 특정 사용자의 모든 알림을 읽음 상태로 변경합니다.
     * 
     * @param userId 사용자 ID
     */
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다. user_idx: " + userId));
        
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalse(user);
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }
    
    /**
     * 거래 관련 알림을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param transactionId 거래 ID
     * @param message 알림 메시지
     * @return 생성된 알림
     */
    public Notification createTransactionNotification(Long userId, Long transactionId, String message) {
        return createNotification(
            userId, 
            "거래 알림", 
            message, 
            "TRANSACTION", 
            transactionId
        );
    }
    
    /**
     * 시스템 알림을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param message 알림 메시지
     * @return 생성된 알림
     */
    public Notification createSystemNotification(Long userId, String message) {
        return createNotification(
            userId, 
            "시스템 알림", 
            message, 
            "SYSTEM", 
            null
        );
    }
} 
package com.example.AssetTrading.Repository;

import com.example.AssetTrading.Entity.FileStorage;
import com.example.AssetTrading.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileStorageRepository extends JpaRepository<FileStorage, Long> {
    
    /**
     * 특정 엔티티 타입과 ID에 연결된 파일 목록 조회 (삭제되지 않은 것만)
     */
    List<FileStorage> findByEntityTypeAndEntityIdAndIsDeletedFalse(String entityType, Long entityId);
    
    /**
     * 특정 업로더가 업로드한 파일 목록 조회 (삭제되지 않은 것만)
     */
    List<FileStorage> findByUploaderAndIsDeletedFalse(User uploader);
    
    /**
     * 특정 파일 타입의 파일 목록 조회 (삭제되지 않은 것만)
     */
    List<FileStorage> findByFileTypeContainingAndIsDeletedFalse(String fileType);
    
    /**
     * 특정 엔티티 타입에 연결된 파일 목록 조회 (삭제되지 않은 것만)
     */
    List<FileStorage> findByEntityTypeAndIsDeletedFalse(String entityType);
    
    /**
     * 소프트 딜리트된 파일 목록 조회
     */
    List<FileStorage> findByIsDeletedTrue();
    
    /**
     * 특정 시간 이전에 생성된 특정 엔티티 타입의 파일 목록 조회
     */
    List<FileStorage> findByEntityTypeAndCreatedAtBefore(String entityType, LocalDateTime createdAt);
} 
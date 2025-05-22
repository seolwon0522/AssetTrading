package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_storages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_idx")
    private Long id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename; // 원본 파일명

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename; // 저장된 파일명

    @Column(name = "file_path", nullable = false)
    private String filePath; // 파일 경로

    @Column(name = "file_size", nullable = false)
    private Long fileSize; // 파일 크기

    @Column(name = "file_type", nullable = false)
    private String fileType; // 파일 유형

    @Column(name = "entity_type")
    private String entityType; // 연결된 엔티티 타입 (USER, PRODUCT 등)

    @Column(name = "entity_id")
    private Long entityId; // 연결된 엔티티 ID

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 업로드 시간

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private Boolean isDeleted; // 삭제 여부
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_idx")
    private User uploader; // 업로더 정보
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isDeleted == null) {
            isDeleted = false;
        }
    }
} 
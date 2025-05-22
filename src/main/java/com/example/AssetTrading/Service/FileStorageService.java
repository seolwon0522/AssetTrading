package com.example.AssetTrading.Service;

import com.example.AssetTrading.Entity.FileStorage;
import com.example.AssetTrading.Entity.User;
import com.example.AssetTrading.Exception.ResourceNotFoundException;
import com.example.AssetTrading.Repository.FileStorageRepository;
import com.example.AssetTrading.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileStorageService {

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;
    
    // 허용된 파일 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".zip"
    );
    
    // 최대 파일 크기 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final FileStorageRepository fileStorageRepository;
    private final UserRepository userRepository;

    /**
     * 파일 업로드
     * 
     * @param file 업로드할 파일
     * @param entityType 연결된 엔티티 타입 (USER, PRODUCT, CHAT 등)
     * @param entityId 연결된 엔티티 ID
     * @param uploaderId 업로더 ID
     * @return 저장된 파일 정보
     * @throws IOException 파일 저장 중 오류 발생 시
     * @throws IllegalArgumentException 유효하지 않은 파일인 경우
     */
    public FileStorage uploadFile(MultipartFile file, String entityType, Long entityId, Long uploaderId) throws IOException {
        log.debug("파일 업로드 요청: filename={}, entityType={}, entityId={}, uploader={}", 
                 file.getOriginalFilename(), entityType, entityId, uploaderId);
        
        // null 체크
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        
        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 최대 허용 크기(" + MAX_FILE_SIZE / (1024 * 1024) + "MB)를 초과합니다.");
        }
        
        // 업로더 확인
        User uploader = userRepository.findById(uploaderId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다. user_idx: " + uploaderId));
        
        // 파일명 가져오기
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        
        // 파일명 검증
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("파일명에 잘못된 경로가 포함되어 있습니다: " + originalFilename);
        }
        
        // 파일 확장자 검증
        String fileExtension = getFileExtension(originalFilename);
        if (!isAllowedExtension(fileExtension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + fileExtension);
        }
        
        // 저장할 파일명 생성 (UUID + 원본 확장자)
        String storedFilename = UUID.randomUUID().toString() + fileExtension;
        
        // 파일 저장 디렉토리 경로 생성 (연/월/일 구조)
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String directoryPath = uploadDir + "/" + entityType.toLowerCase() + "/" + datePath;
        Path storageDirectory = Paths.get(directoryPath);
        
        try {
            // 디렉토리가 없으면 생성
            if (!Files.exists(storageDirectory)) {
                Files.createDirectories(storageDirectory);
                log.debug("디렉토리 생성: {}", storageDirectory);
            }
            
            // 파일 저장
            Path targetLocation = storageDirectory.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.debug("파일 저장 완료: {}", targetLocation);
            
            // 파일 정보 DB 저장
            FileStorage fileStorage = FileStorage.builder()
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .filePath(directoryPath + "/" + storedFilename)
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .entityType(entityType)
                .entityId(entityId)
                .uploader(uploader)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();
            
            FileStorage savedFile = fileStorageRepository.save(fileStorage);
            log.info("파일 업로드 완료: id={}, filename={}, size={}KB", 
                    savedFile.getId(), savedFile.getOriginalFilename(), savedFile.getFileSize() / 1024);
            
            return savedFile;
        } catch (IOException e) {
            log.error("파일 저장 중 오류 발생: {}", e.getMessage(), e);
            throw new IOException("파일 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 파일 삭제 (소프트 딜리트)
     * 
     * @param fileId 파일 ID
     * @param userId 사용자 ID (권한 확인용)
     * @throws ResourceNotFoundException 파일이 존재하지 않는 경우
     * @throws IllegalArgumentException 권한이 없는 경우
     */
    public void deleteFile(Long fileId, Long userId) {
        log.debug("파일 삭제 요청: fileId={}, userId={}", fileId, userId);
        
        FileStorage fileStorage = fileStorageRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 파일이 존재하지 않습니다. file_idx: " + fileId));
        
        // 이미 삭제된 파일인지 확인
        if (fileStorage.getIsDeleted()) {
            log.warn("이미 삭제된 파일입니다: fileId={}", fileId);
            return;
        }
        
        // 파일 업로더 확인 (권한 검사)
        if (!fileStorage.getUploader().getUser_idx().equals(userId)) {
            log.warn("파일 삭제 권한 없음: fileId={}, userId={}, uploaderId={}", 
                    fileId, userId, fileStorage.getUploader().getUser_idx());
            throw new IllegalArgumentException("파일 업로더만 삭제할 수 있습니다.");
        }
        
        // 소프트 딜리트 처리
        fileStorage.setIsDeleted(true);
        fileStorageRepository.save(fileStorage);
        log.info("파일 삭제 완료(소프트 딜리트): fileId={}, filename={}", 
                fileId, fileStorage.getOriginalFilename());
    }
    
    /**
     * 실제 파일 삭제 (하드 딜리트) - 관리자 전용
     * 
     * @param fileId 파일 ID
     * @throws ResourceNotFoundException 파일이 존재하지 않는 경우
     * @throws IOException 파일 삭제 중 오류 발생 시
     */
    @Transactional
    public void hardDeleteFile(Long fileId) throws IOException {
        log.debug("파일 영구 삭제 요청: fileId={}", fileId);
        
        FileStorage fileStorage = fileStorageRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 파일이 존재하지 않습니다. file_idx: " + fileId));
        
        try {
            // 파일 실제 삭제
            Path filePath = Paths.get(fileStorage.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.debug("실제 파일 삭제 완료: {}", filePath);
            } else {
                log.warn("파일이 존재하지 않음: {}", filePath);
            }
            
            // DB에서도 삭제
            fileStorageRepository.delete(fileStorage);
            log.info("파일 영구 삭제 완료: fileId={}, filename={}", 
                    fileId, fileStorage.getOriginalFilename());
        } catch (IOException e) {
            log.error("파일 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new IOException("파일 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 특정 엔티티와 연결된 파일 목록 조회
     * 
     * @param entityType 엔티티 타입
     * @param entityId 엔티티 ID
     * @return 파일 목록
     */
    @Transactional(readOnly = true)
    public List<FileStorage> getFilesByEntity(String entityType, Long entityId) {
        log.debug("엔티티 관련 파일 조회: entityType={}, entityId={}", entityType, entityId);
        
        List<FileStorage> files = fileStorageRepository.findByEntityTypeAndEntityIdAndIsDeletedFalse(entityType, entityId);
        log.debug("파일 조회 결과: count={}", files.size());
        
        return files;
    }
    
    /**
     * 파일 상세 정보 조회
     * 
     * @param fileId 파일 ID
     * @return 파일 정보
     * @throws ResourceNotFoundException 파일이 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    public FileStorage getFile(Long fileId) {
        log.debug("파일 상세 정보 조회: fileId={}", fileId);
        
        FileStorage file = fileStorageRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 파일이 존재하지 않습니다. file_idx: " + fileId));
        
        // 소프트 딜리트된 파일인지 확인
        if (file.getIsDeleted()) {
            log.warn("삭제된 파일 조회 시도: fileId={}", fileId);
            throw new ResourceNotFoundException("삭제된 파일입니다.");
        }
        
        return file;
    }
    
    /**
     * 파일 다운로드를 위한 Path 객체 반환
     * 
     * @param fileId 파일 ID
     * @return 파일 경로
     * @throws ResourceNotFoundException 파일이 존재하지 않는 경우
     * @throws IOException 파일을 읽을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Path getFilePath(Long fileId) throws IOException {
        log.debug("파일 경로 조회: fileId={}", fileId);
        
        FileStorage fileStorage = getFile(fileId);
        Path filePath = Paths.get(fileStorage.getFilePath());
        
        if (!Files.exists(filePath)) {
            log.error("파일이 디스크에 존재하지 않음: {}", filePath);
            throw new IOException("파일을 찾을 수 없습니다: " + fileStorage.getOriginalFilename());
        }
        
        return filePath;
    }
    
    /**
     * 임시 파일 업로드 (엔티티와 연결되지 않은 임시 파일)
     * 
     * @param file 업로드할 파일
     * @param uploaderId 업로더 ID
     * @return 저장된 파일 정보
     * @throws IOException 파일 저장 중 오류 발생 시
     */
    public FileStorage uploadTempFile(MultipartFile file, Long uploaderId) throws IOException {
        log.debug("임시 파일 업로드 요청: filename={}, uploaderId={}", file.getOriginalFilename(), uploaderId);
        return uploadFile(file, "TEMP", 0L, uploaderId);
    }
    
    /**
     * 임시 파일을 특정 엔티티와 연결 (임시 파일을 실제 사용)
     * 
     * @param fileId 파일 ID
     * @param entityType 연결할 엔티티 타입
     * @param entityId 연결할 엔티티 ID
     * @return 업데이트된 파일 정보
     * @throws ResourceNotFoundException 파일이 존재하지 않는 경우
     */
    @Transactional
    public FileStorage linkFileToEntity(Long fileId, String entityType, Long entityId) {
        log.debug("임시 파일 연결 요청: fileId={}, entityType={}, entityId={}", fileId, entityType, entityId);
        
        FileStorage fileStorage = fileStorageRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 파일이 존재하지 않습니다. file_idx: " + fileId));
        
        // 삭제된 파일인지 확인
        if (fileStorage.getIsDeleted()) {
            log.warn("삭제된 파일 연결 시도: fileId={}", fileId);
            throw new IllegalArgumentException("삭제된 파일은 연결할 수 없습니다.");
        }
        
        // 임시 파일인지 확인
        if (!"TEMP".equals(fileStorage.getEntityType())) {
            log.warn("이미 연결된 파일: fileId={}, currentEntityType={}", fileId, fileStorage.getEntityType());
            // 이미 같은 엔티티에 연결되어 있는지 확인
            if (entityType.equals(fileStorage.getEntityType()) && entityId.equals(fileStorage.getEntityId())) {
                return fileStorage; // 이미 원하는 엔티티에 연결되어 있으면 그대로 반환
            }
            throw new IllegalArgumentException("임시 파일이 아닙니다. 이미 다른 엔티티에 연결되어 있습니다.");
        }
        
        // 엔티티 연결
        fileStorage.setEntityType(entityType);
        fileStorage.setEntityId(entityId);
        
        FileStorage updatedFile = fileStorageRepository.save(fileStorage);
        log.info("임시 파일 연결 완료: fileId={}, entityType={}, entityId={}", 
                fileId, entityType, entityId);
        
        return updatedFile;
    }
    
    /**
     * 사용자별 파일 목록 조회
     * 
     * @param uploaderId 업로더 ID
     * @return 파일 목록
     */
    @Transactional(readOnly = true)
    public List<FileStorage> getFilesByUploader(Long uploaderId) {
        log.debug("사용자 업로드 파일 조회: uploaderId={}", uploaderId);
        
        User uploader = userRepository.findById(uploaderId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다. user_idx: " + uploaderId));
        
        List<FileStorage> files = fileStorageRepository.findByUploaderAndIsDeletedFalse(uploader);
        log.debug("파일 조회 결과: count={}", files.size());
        
        return files;
    }
    
    /**
     * 파일 타입별 파일 목록 조회
     * 
     * @param fileType 파일 타입 (MIME 타입)
     * @return 파일 목록
     */
    @Transactional(readOnly = true)
    public List<FileStorage> getFilesByType(String fileType) {
        log.debug("파일 타입별 조회: fileType={}", fileType);
        
        List<FileStorage> files = fileStorageRepository.findByFileTypeContainingAndIsDeletedFalse(fileType);
        log.debug("파일 조회 결과: count={}", files.size());
        
        return files;
    }
    
    /**
     * 일정 기간 이상 지난 임시 파일 정리
     * 
     * @param days 보관 기간 (일)
     * @return 정리된 파일 수
     */
    @Transactional
    public int cleanupTempFiles(int days) {
        log.info("임시 파일 정리 시작: days={}", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<FileStorage> tempFiles = fileStorageRepository.findByEntityTypeAndCreatedAtBefore("TEMP", cutoffDate);
        
        int count = 0;
        for (FileStorage file : tempFiles) {
            try {
                hardDeleteFile(file.getId());
                count++;
            } catch (Exception e) {
                log.error("임시 파일 삭제 중 오류: fileId={}, error={}", file.getId(), e.getMessage(), e);
            }
        }
        
        log.info("임시 파일 정리 완료: 총 {}개 삭제됨", count);
        return count;
    }
    
    /**
     * 파일 경로에서 파일 확장자 추출
     * 
     * @param filename 파일명
     * @return 파일 확장자 (점 포함)
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty() || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
    
    /**
     * 허용된 파일 확장자인지 확인
     * 
     * @param extension 파일 확장자
     * @return 허용 여부
     */
    private boolean isAllowedExtension(String extension) {
        return extension != null && !extension.isEmpty() && ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }
    
    /**
     * 파일이 존재하는지 확인
     * 
     * @param filePath 파일 경로
     * @return 존재 여부
     */
    public boolean isFileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * 파일 복제
     * 
     * @param sourceFileId 원본 파일 ID
     * @param targetEntityType 대상 엔티티 타입
     * @param targetEntityId 대상 엔티티 ID
     * @return 복제된 파일 정보
     */
    @Transactional
    public FileStorage duplicateFile(Long sourceFileId, String targetEntityType, Long targetEntityId) {
        log.debug("파일 복제 요청: sourceFileId={}, targetEntityType={}, targetEntityId={}", 
                 sourceFileId, targetEntityType, targetEntityId);
        
        // 원본 파일 조회
        FileStorage sourceFile = getFile(sourceFileId);
        
        try {
            // 파일 복사
            Path sourcePath = Paths.get(sourceFile.getFilePath());
            
            // 새 파일명 생성
            String newStoredFilename = UUID.randomUUID().toString() + getFileExtension(sourceFile.getOriginalFilename());
            
            // 새 파일 경로 생성
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String directoryPath = uploadDir + "/" + targetEntityType.toLowerCase() + "/" + datePath;
            Path targetDirectory = Paths.get(directoryPath);
            
            // 디렉토리 생성
            if (!Files.exists(targetDirectory)) {
                Files.createDirectories(targetDirectory);
            }
            
            // 파일 복사
            Path targetPath = targetDirectory.resolve(newStoredFilename);
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 새 파일 엔티티 생성
            FileStorage newFile = FileStorage.builder()
                .originalFilename(sourceFile.getOriginalFilename())
                .storedFilename(newStoredFilename)
                .filePath(directoryPath + "/" + newStoredFilename)
                .fileSize(sourceFile.getFileSize())
                .fileType(sourceFile.getFileType())
                .entityType(targetEntityType)
                .entityId(targetEntityId)
                .uploader(sourceFile.getUploader())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();
                
            FileStorage savedFile = fileStorageRepository.save(newFile);
            log.info("파일 복제 완료: sourceId={}, newId={}", sourceFileId, savedFile.getId());
            
            return savedFile;
        } catch (IOException e) {
            log.error("파일 복제 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("파일 복제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
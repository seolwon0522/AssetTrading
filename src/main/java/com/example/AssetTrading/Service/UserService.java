package com.example.AssetTrading.Service;

import com.example.AssetTrading.Dto.LoginRequestDto;
import com.example.AssetTrading.Dto.UserRequestDto;
import com.example.AssetTrading.Dto.UserResponseDto;
import com.example.AssetTrading.Entity.User;
import com.example.AssetTrading.Exception.AuthenticationException;
import com.example.AssetTrading.Exception.DuplicateResourceException;
import com.example.AssetTrading.Exception.ResourceNotFoundException;
import com.example.AssetTrading.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final BusinessNumCheckService businessNumCheckService;
    private final NotificationService notificationService;
    
    private static final String LOGIN_USER = "LOGIN_USER";
    private static final int PASSWORD_MIN_LENGTH = 8;

    /**
     * 사용자 회원가입
     * 
     * @param dto 회원가입 요청 정보
     * @return 생성된 사용자 정보
     * @throws DuplicateResourceException 이미 존재하는 사용자 ID인 경우
     * @throws IllegalArgumentException 유효하지 않은 입력값인 경우
     */
    public UserResponseDto register(UserRequestDto dto) {
        log.info("신규 사용자 회원가입 요청: {}", dto.getUserId());
        
        // 입력값 유효성 검사
        validateUserInput(dto);
        
        // 사업자등록번호 유효성 검증
        boolean businessNumValid = businessNumCheckService.checkBusinessNum(
            dto.getBusinessNum(),
            dto.getUserName(),
            dto.getStartDate(),
            dto.getCompanyName()
        );
        
        if (!businessNumValid) {
            log.warn("사업자등록번호 유효성 검증 실패: {}", dto.getBusinessNum());
            throw new IllegalArgumentException("사업자등록 진위확인에 실패했습니다.");
        }
        
        // 새 사용자 생성
        User user = dto.toEntity();
        // TODO: 실제 운영 환경에서는 PasswordEncoder를 사용하여 암호화 필요
        user.setJoinApproved(false); // 기본적으로 승인 대기 상태로 설정
        user.setRegisteredAt(LocalDateTime.now());
        
        // 사용자 저장
        User savedUser = userRepository.save(user);
        log.info("사용자 등록 완료: {}, idx: {}", savedUser.getUserId(), savedUser.getUser_idx());
        
        return UserResponseDto.fromEntity(savedUser);
    }

    /**
     * 사용자 ID로 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자 정보
     * @throws ResourceNotFoundException 사용자가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다. userId: " + userId));
        
        return UserResponseDto.fromEntity(user);
    }

    /**
     * 사용자 인덱스로 조회
     * 
     * @param userIdx 사용자 인덱스
     * @return 사용자 정보
     * @throws ResourceNotFoundException 사용자가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserByUserIdx(Long userIdx) {
        User user = userRepository.findById(userIdx)
            .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다. user_idx: " + userIdx));
        
        return UserResponseDto.fromEntity(user);
    }

    /**
     * 로그인 처리
     * 
     * @param loginDto 로그인 요청 정보
     * @param session HTTP 세션
     * @throws AuthenticationException 인증 실패 시
     */
    public void login(LoginRequestDto loginDto, HttpSession session) {
        log.info("로그인 시도: {}", loginDto.getUserId());
        
        // 사용자 조회
        User user = userRepository.findByUserId(loginDto.getUserId())
            .orElseThrow(() -> new AuthenticationException("아이디 또는 비밀번호가 올바르지 않습니다."));
        
        // 비밀번호 검증 (평문 비교)
        // TODO: 실제 운영 환경에서는 PasswordEncoder를 사용하여 암호화된 비밀번호 비교 필요
        if (!user.getUserPw().equals(loginDto.getUserPw())) {
            log.warn("비밀번호 불일치: {}", loginDto.getUserId());
            throw new AuthenticationException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        
        // 가입 승인 여부 확인
        if (!user.isJoinApproved()) {
            log.warn("미승인 사용자 로그인 시도: {}", loginDto.getUserId());
            throw new AuthenticationException("관리자 승인 대기 중입니다. 승인 후 로그인이 가능합니다.");
        }
        
        // 세션에 사용자 정보 저장
        session.setAttribute(LOGIN_USER, UserResponseDto.fromEntity(user));
        log.info("로그인 성공: {}", user.getUserId());
        
        // 로그인 알림 생성
        notificationService.createSystemNotification(user.getUser_idx(), "로그인이 완료되었습니다.");
    }

    /**
     * 로그아웃 처리
     * 
     * @param session HTTP 세션
     */
    public void logout(HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute(LOGIN_USER);
        if (user != null) {
            log.info("로그아웃: {}", user.getUserId());
        }
        session.removeAttribute(LOGIN_USER);
        session.invalidate(); // 세션 자체를 무효화
    }
    
    /**
     * 비밀번호 변경
     * 
     * @param userIdx 사용자 인덱스
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     * @throws AuthenticationException 현재 비밀번호가 일치하지 않는 경우
     * @throws ResourceNotFoundException 사용자가 존재하지 않는 경우
     */
    public void changePassword(Long userIdx, String currentPassword, String newPassword) {
        // 사용자 조회
        User user = userRepository.findById(userIdx)
            .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다. user_idx: " + userIdx));
        
        // 현재 비밀번호 검증 (평문 비교)
        // TODO: 실제 운영 환경에서는 PasswordEncoder를 사용하여 암호화된 비밀번호 비교 필요
        if (!user.getUserPw().equals(currentPassword)) {
            throw new AuthenticationException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        // 새 비밀번호 유효성 검사
        if (newPassword == null || newPassword.length() < PASSWORD_MIN_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 " + PASSWORD_MIN_LENGTH + "자 이상이어야 합니다.");
        }
        
        // 새 비밀번호 저장 (평문)
        // TODO: 실제 운영 환경에서는 PasswordEncoder를 사용하여 암호화 필요
        user.setUserPw(newPassword);
        userRepository.save(user);
        
        log.info("비밀번호 변경 완료: {}", user.getUserId());
        
        // 비밀번호 변경 알림 생성
        notificationService.createSystemNotification(user.getUser_idx(), "비밀번호가 변경되었습니다.");
    }
    
    /**
     * 회사 정보 업데이트
     * 
     * @param userIdx 사용자 인덱스
     * @param dto 업데이트할 정보
     * @return 업데이트된 사용자 정보
     */
    public UserResponseDto updateCompanyInfo(Long userIdx, UserRequestDto dto) {
        // 사용자 조회
        User user = userRepository.findById(userIdx)
            .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다. user_idx: " + userIdx));
        
        // 업데이트 가능한 필드만 업데이트
        if (dto.getCompanyName() != null) {
            user.setCompanyName(dto.getCompanyName());
        }
        
        if (dto.getCompanyAddress() != null) {
            user.setCompanyAddress(dto.getCompanyAddress());
        }
        
        if (dto.getCompanyIndustry() != null) {
            user.setCompanyIndustry(dto.getCompanyIndustry());
        }
        
        if (dto.getCompanyTel() != null) {
            user.setCompanyTel(dto.getCompanyTel());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("회사 정보 업데이트 완료: {}", user.getUserId());
        
        return UserResponseDto.fromEntity(updatedUser);
    }
    
    /**
     * 관리자: 회원가입 승인
     * 
     * @param userIdx 사용자 인덱스
     * @param approved 승인 여부
     * @return 업데이트된 사용자 정보
     */
    public UserResponseDto approveUserRegistration(Long userIdx, boolean approved) {
        // 사용자 조회
        User user = userRepository.findById(userIdx)
            .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다. user_idx: " + userIdx));
        
        // 승인 상태 업데이트
        user.setJoinApproved(approved);
        User updatedUser = userRepository.save(user);
        
        // 알림 생성
        String message = approved 
            ? "회원가입이 승인되었습니다. 이제 서비스를 이용하실 수 있습니다."
            : "회원가입이 거부되었습니다. 자세한 사항은 관리자에게 문의하세요.";
            
        notificationService.createSystemNotification(user.getUser_idx(), message);
        
        log.info("사용자 승인 상태 변경: userId={}, approved={}", user.getUserId(), approved);
        
        return UserResponseDto.fromEntity(updatedUser);
    }
    
    /**
     * 미승인 사용자 목록 조회
     * 
     * @return 미승인 사용자 목록
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getUnapprovedUsers() {
        List<User> users = userRepository.findByJoinApprovedFalse();
        
        return users.stream()
            .map(UserResponseDto::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * 입력값 유효성 검사
     */
    private void validateUserInput(UserRequestDto dto) {
        // 아이디 검사
        if (dto.getUserId() == null || dto.getUserId().isBlank()) {
            throw new IllegalArgumentException("아이디를 입력하세요.");
        }
        
        // 아이디 중복 검사
        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new DuplicateResourceException("이미 사용 중인 아이디입니다.");
        }
        
        // 비밀번호 검사
        if (dto.getUserPw() == null || dto.getUserPw().length() < PASSWORD_MIN_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 " + PASSWORD_MIN_LENGTH + "자 이상이어야 합니다.");
        }
        
        // 이름 검사
        if (dto.getUserName() == null || dto.getUserName().isBlank()) {
            throw new IllegalArgumentException("이름을 입력하세요.");
        }
        
        // 사업자번호 검사
        if (dto.getBusinessNum() == null || dto.getBusinessNum().isBlank()) {
            throw new IllegalArgumentException("사업자등록번호를 입력하세요.");
        }
        
        // 회사명 검사
        if (dto.getCompanyName() == null || dto.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("회사명을 입력하세요.");
        }
    }
}

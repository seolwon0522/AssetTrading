package com.example.AssetTrading.Controller;

import com.example.AssetTrading.Dto.LoginRequestDto;
import com.example.AssetTrading.Dto.UserRequestDto;
import com.example.AssetTrading.Dto.UserResponseDto;
import com.example.AssetTrading.Exception.AuthenticationException;
import com.example.AssetTrading.Exception.DuplicateResourceException;
import com.example.AssetTrading.Exception.ResourceNotFoundException;
import com.example.AssetTrading.Service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    /**
     * 사용자 회원가입
     * 
     * @param userRequestDto 회원가입 요청 정보
     * @return 생성된 사용자 정보
     */
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody UserRequestDto userRequestDto) {
        log.info("회원가입 요청: {}", userRequestDto.getUserId());
        
        try {
            UserResponseDto responseDto = userService.register(userRequestDto);
            log.info("회원가입 성공: {}", responseDto.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (DuplicateResourceException e) {
            log.warn("회원가입 실패 - 중복 리소스: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패 - 유효하지 않은 입력: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "회원가입 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 사용자 정보 조회
     * 
     * @param userIdx 사용자 인덱스
     * @return 사용자 정보
     */
    @GetMapping("/{userIdx}")
    public ResponseEntity<Object> getUserByUserIdx(@PathVariable("userIdx") Long userIdx) {
        log.info("사용자 정보 조회 요청: userIdx={}", userIdx);
        
        try {
            UserResponseDto responseDto = userService.getUserByUserIdx(userIdx);
            return ResponseEntity.ok(responseDto);
        } catch (ResourceNotFoundException e) {
            log.warn("사용자 정보 조회 실패 - 리소스 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "사용자 정보 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 사용자 ID로 정보 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    @GetMapping("/by-userid/{userId}")
    public ResponseEntity<Object> getUserByUserId(@PathVariable("userId") String userId) {
        log.info("사용자 정보 조회 요청: userId={}", userId);
        
        try {
            UserResponseDto responseDto = userService.getUserByUserId(userId);
            return ResponseEntity.ok(responseDto);
        } catch (ResourceNotFoundException e) {
            log.warn("사용자 정보 조회 실패 - 리소스 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "사용자 정보 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 로그인 처리
     * 
     * @param loginDto 로그인 요청 정보
     * @param session HTTP 세션
     * @return 로그인 결과
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequestDto loginDto, HttpSession session) {
        log.info("로그인 요청: {}", loginDto.getUserId());
        
        try {
            userService.login(loginDto, session);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그인 성공"
            ));
        } catch (AuthenticationException e) {
            log.warn("로그인 실패 - 인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("로그인 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "로그인 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 로그아웃 처리
     * 
     * @param session HTTP 세션
     * @return 로그아웃 결과
     */
    @PostMapping("/logout")
    public ResponseEntity<Object> logout(HttpSession session) {
        log.info("로그아웃 요청");
        
        try {
            userService.logout(session);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그아웃 성공"
            ));
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "로그아웃 처리 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 비밀번호 변경
     * 
     * @param userIdx 사용자 인덱스
     * @param passwordData 비밀번호 변경 정보
     * @return 변경 결과
     */
    @PutMapping("/{userIdx}/password")
    public ResponseEntity<Object> changePassword(
            @PathVariable("userIdx") Long userIdx,
            @RequestBody Map<String, String> passwordData) {
        
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        
        log.info("비밀번호 변경 요청: userIdx={}", userIdx);
        
        try {
            userService.changePassword(userIdx, currentPassword, newPassword);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "비밀번호가 성공적으로 변경되었습니다."
            ));
        } catch (ResourceNotFoundException e) {
            log.warn("비밀번호 변경 실패 - 리소스 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (AuthenticationException e) {
            log.warn("비밀번호 변경 실패 - 인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 변경 실패 - 유효하지 않은 입력: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("비밀번호 변경 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "비밀번호 변경 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 회사 정보 업데이트
     * 
     * @param userIdx 사용자 인덱스
     * @param requestDto 업데이트할 정보
     * @return 업데이트된 사용자 정보
     */
    @PutMapping("/{userIdx}/company-info")
    public ResponseEntity<Object> updateCompanyInfo(
            @PathVariable("userIdx") Long userIdx,
            @RequestBody UserRequestDto requestDto) {
        
        log.info("회사 정보 업데이트 요청: userIdx={}", userIdx);
        
        try {
            UserResponseDto responseDto = userService.updateCompanyInfo(userIdx, requestDto);
            return ResponseEntity.ok(responseDto);
        } catch (ResourceNotFoundException e) {
            log.warn("회사 정보 업데이트 실패 - 리소스 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("회사 정보 업데이트 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "회사 정보 업데이트 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 관리자용: 회원가입 승인
     * 
     * @param userIdx 사용자 인덱스
     * @param approved 승인 여부
     * @return 업데이트된 사용자 정보
     */
    @PutMapping("/admin/approve/{userIdx}")
    public ResponseEntity<Object> approveUserRegistration(
            @PathVariable("userIdx") Long userIdx,
            @RequestParam("approved") boolean approved) {
        
        log.info("회원가입 승인 요청: userIdx={}, approved={}", userIdx, approved);
        
        try {
            UserResponseDto responseDto = userService.approveUserRegistration(userIdx, approved);
            return ResponseEntity.ok(responseDto);
        } catch (ResourceNotFoundException e) {
            log.warn("회원가입 승인 실패 - 리소스 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("회원가입 승인 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "회원가입 승인 처리 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 관리자용: 미승인 사용자 목록 조회
     * 
     * @return 미승인 사용자 목록
     */
    @GetMapping("/admin/unapproved")
    public ResponseEntity<Object> getUnapprovedUsers() {
        log.info("미승인 사용자 목록 조회 요청");
        
        try {
            List<UserResponseDto> users = userService.getUnapprovedUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("미승인 사용자 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "미승인 사용자 목록 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 전역 예외 처리
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("리소스를 찾을 수 없음: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateResourceException(DuplicateResourceException e) {
        log.warn("중복된 리소스: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException e) {
        log.warn("인증 오류: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", e.getMessage()));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("유효하지 않은 입력값: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }
}
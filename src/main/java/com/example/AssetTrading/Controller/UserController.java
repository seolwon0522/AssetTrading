package com.example.AssetTrading.Controller;

import com.example.AssetTrading.Dto.LoginRequestDto;
import com.example.AssetTrading.Dto.UserRequestDto;
import com.example.AssetTrading.Dto.UserResponseDto;
import com.example.AssetTrading.Service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    // 사용자 회원가입
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.ok(userService.register(userRequestDto));
    }

    // 사용자 정보 조회
    @GetMapping("/{user_idx}")
    public ResponseEntity<UserResponseDto> getUserByUserIdx(@PathVariable("user_idx") Long user_idx) {
        return ResponseEntity.ok(userService.getUserByUserIdx(user_idx));
    }

    // 로그인 처리
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginDto, HttpSession session) {
        userService.login(loginDto, session);
        return ResponseEntity.ok("로그인 성공");
    }

    // 로그아웃 처리
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        userService.logout(session);
        return ResponseEntity.ok("로그아웃 성공");
    }
}
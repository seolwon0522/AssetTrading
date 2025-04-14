package com.example.AssetTrading.Service;

import com.example.AssetTrading.Dto.LoginRequestDto;
import com.example.AssetTrading.Dto.UserRequestDto;
import com.example.AssetTrading.Dto.UserResponseDto;
import com.example.AssetTrading.Entity.User;
import com.example.AssetTrading.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private static final String LOGIN_USER = "LOGIN_USER";

    public UserResponseDto register(UserRequestDto userRequestDto) {
        if (userRequestDto.getUserId() == null || userRequestDto.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("이메일을 입력하세요");
        }

        if (userRequestDto.getUserPw() == null || userRequestDto.getUserPw().trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력 하세요");
        }

        if (userRepository.existsByUserId(userRequestDto.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User savedUser = userRepository.save(userRequestDto.toEntity());
        return UserResponseDto.fromEntity(savedUser);
    }


    public UserResponseDto getNicknameById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return UserResponseDto.fromEntity(user);
    }

    public void login(LoginRequestDto loginDto, HttpSession session) {
        User user = userRepository.findByUserId(loginDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("이메일이 존재하지 않습니다."));

        if (!user.getUserPw().equals(loginDto.getUserPw())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        session.setAttribute(LOGIN_USER, UserResponseDto.fromEntity(user));
    }

    public void logout(HttpSession session) {
        session.removeAttribute(LOGIN_USER);
    }
}

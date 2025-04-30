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

@Service                       /// Service 부분
@RequiredArgsConstructor       /// private final 로 선언 받은 필드 값들 생성자 생략 가능
@Transactional                 /// 값들이 DB로 넘어가는데에 있어서 문제 발생 시 값이 안 넘어감
public class UserService {
    private final UserRepository userRepository;               /// userRepository user의 임시저장소를 값 초기화
    private static final String LOGIN_USER = "LOGIN_USER";     /// ? 흠

    // 회원가입 관련 메소드
    public UserResponseDto register(UserRequestDto userRequestDto) {  ///  회원가입 당시에 userRequestDto 타입으로 반환

        // 만약, userRequestDto 안의 UserId 를 가져왔을 때,
        // 그 값이 null 값이거나? 공백이라면?
        // 예외 객체 생성 -> throw new IllegalArgumentException("이메일을 입력하세요");
        if (userRequestDto.getUserId() == null || userRequestDto.getUserId().isBlank()) {
            throw new IllegalArgumentException("이메일을 입력하세요");
        }

        // 만약, userRepository 안의 있는 UserId를 가져왔을 때,
        // 그 userId가 이미 DB에 존재한다면,
        // 예외 객체 생성 -> throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        // 그 userId가 DB에 존재하지 않는다면,
        // 회원가입 당시 입력한 값인 userRequestDto json -> dto의 toEntity java 객체 형태로 저장하여 그 값을 savedUser 넣음
        // savedUser를 UserResponseDto의 fromEntity를 통해 회원의 정보를
        if (userRepository.existsByUserId(userRequestDto.getUserId())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        User savedUser = userRepository.save(userRequestDto.toEntity());
        return UserResponseDto.fromEntity(savedUser);
    }

    // 사용자 찾는 메소드
    public UserResponseDto getUserByUserIdx(Long user_idx) { ///  user_idx 키 값을 받음
        // userRepository 안에 입력받은 user_idx 값을 찾아서 user 로 둘 것
        User user = userRepository.findById(user_idx)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다. user_idx=" + user_idx));
        return UserResponseDto.fromEntity(user);
    }

    public void login(LoginRequestDto loginDto, HttpSession session) {
        User user = userRepository.findByUserId(loginDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("이메일이 존재하지 않습니다"));

        if (!user.getUserPw().equals(loginDto.getUserPw())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
        session.setAttribute(LOGIN_USER, UserResponseDto.fromEntity(user));
    }

    public void logout(HttpSession session) {
        session.removeAttribute(LOGIN_USER);
    }
}

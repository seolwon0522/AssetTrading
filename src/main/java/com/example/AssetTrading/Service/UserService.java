package com.example.AssetTrading.Service;

import com.example.AssetTrading.Dto.UserRequestDto;
import com.example.AssetTrading.Entity.User;
import com.example.AssetTrading.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    //회원가입 로직 ( 중복 아이디(이메일) X )
    public void register(UserRequestDto userRequestDto) {
        if(userRepository.existsByEmail(userRequestDto.getEmail())){
            throw new IllegalArgumentException("사용중인이메일");
        }
    }

    //로그인 ( 없는 id , 비밀번호 불일치 예외처리 )
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email) //findby는 optional<user> 리턴
                .orElseThrow(()->new IllegalArgumentException("가입된 이메일 없음"));

        if(!user.getPassword().equals(password)){
        throw new IllegalArgumentException("비밀번호 불일치");
        }
        return user;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }


}

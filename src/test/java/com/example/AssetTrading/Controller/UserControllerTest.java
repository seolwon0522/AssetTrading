package com.example.AssetTrading.Controller;

import com.example.AssetTrading.Dto.LoginRequestDto;
import com.example.AssetTrading.Dto.UserRequestDto;
import com.example.AssetTrading.Dto.UserResponseDto;
import com.example.AssetTrading.Exception.AuthenticationException;
import com.example.AssetTrading.Exception.DuplicateResourceException;
import com.example.AssetTrading.Exception.ResourceNotFoundException;
import com.example.AssetTrading.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;
    private LoginRequestDto loginRequestDto;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 설정
        userRequestDto = UserRequestDto.builder()
                .userId("testuser")
                .userPw("password12345")
                .userName("테스트 사용자")
                .businessNum("1234567890")
                .companyName("테스트 회사")
                .companyAddress("서울시 강남구")
                .companyIndustry("IT")
                .companyTel("02-1234-5678")
                .companyEmail("test@example.com")
                .startDate("20200101")
                .build();

        userResponseDto = UserResponseDto.builder()
                .userId("testuser")
                .companyName("테스트 회사")
                .joinApproved(true)
                .build();

        // LoginRequestDto에 빌더가 없는 경우 생성자 사용
        loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUserId("testuser");
        loginRequestDto.setUserPw("password12345");

        session = new MockHttpSession();
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerUserSuccess() throws Exception {
        // Given
        when(userService.register(any(UserRequestDto.class))).thenReturn(userResponseDto);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("testuser"))
                .andExpect(jsonPath("$.companyName").value("테스트 회사"));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 ID 테스트")
    void registerUserFailDuplicateId() throws Exception {
        // Given
        doThrow(new DuplicateResourceException("이미 존재하는 사용자 ID입니다."))
                .when(userService).register(any(UserRequestDto.class));

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("이미 존재하는 사용자 ID입니다."));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginUserSuccess() throws Exception {
        // Given
        doNothing().when(userService).login(any(LoginRequestDto.class), any());

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto))
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인 성공"));
    }

    @Test
    @DisplayName("로그인 실패 - 인증 오류 테스트")
    void loginUserFailAuthentication() throws Exception {
        // Given
        doThrow(new AuthenticationException("로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요."))
                .when(userService).login(any(LoginRequestDto.class), any());

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto))
                .session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요."));
    }

    @Test
    @DisplayName("사용자 정보 조회 성공 테스트")
    void getUserByIdSuccess() throws Exception {
        // Given
        when(userService.getUserByUserId(anyString())).thenReturn(userResponseDto);

        // When & Then
        mockMvc.perform(get("/api/users/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("testuser"))
                .andExpect(jsonPath("$.companyName").value("테스트 회사"));
    }

    @Test
    @DisplayName("사용자 정보 조회 실패 - 사용자 없음 테스트")
    void getUserByIdFail() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("해당 사용자가 존재하지 않습니다. userId: nonexistent"))
                .when(userService).getUserByUserId("nonexistent");

        // When & Then
        mockMvc.perform(get("/api/users/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("해당 사용자가 존재하지 않습니다. userId: nonexistent"));
    }

    @Test
    @DisplayName("모든 사용자 조회 테스트")
    void getAllUsers() throws Exception {
        // Given
        UserResponseDto user1 = UserResponseDto.builder().userId("user1").companyName("회사1").joinApproved(true).build();
        UserResponseDto user2 = UserResponseDto.builder().userId("user2").companyName("회사2").joinApproved(false).build();
        List<UserResponseDto> users = Arrays.asList(user1, user2);
        
        when(userService.getUnapprovedUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users")
                .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user1"))
                .andExpect(jsonPath("$[1].userId").value("user2"));
    }

    @Test
    @DisplayName("로그아웃 테스트")
    void logoutUser() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users/logout")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃 성공"));
    }
} 
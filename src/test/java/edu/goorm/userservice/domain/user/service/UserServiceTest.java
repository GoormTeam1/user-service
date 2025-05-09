package edu.goorm.userservice.domain.user.service;

import edu.goorm.userservice.domain.user.dto.CategoryListRequestDto;
import edu.goorm.userservice.domain.user.entity.Category;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.goorm.userservice.domain.auth.jwt.JwtTokenProvider;
import edu.goorm.userservice.domain.user.dto.TokenDto;
import edu.goorm.userservice.domain.user.dto.UserLoginRequestDto;
import edu.goorm.userservice.domain.user.dto.UserSignupRequestDto;
import edu.goorm.userservice.domain.user.entity.Gender;
import edu.goorm.userservice.domain.user.entity.Level;
import edu.goorm.userservice.domain.user.entity.User;
import edu.goorm.userservice.domain.user.entity.UserInterest;
import edu.goorm.userservice.domain.user.repository.UserInterestRepository;
import edu.goorm.userservice.domain.user.repository.UserRepository;
import edu.goorm.userservice.global.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserInterestRepository userInterestRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @InjectMocks
  private UserService userService;

  private UserSignupRequestDto signupRequest;
  private UserLoginRequestDto loginRequest;
  private User user;
  private List<UserInterest> userInterests;

  @BeforeEach
  void setUp() {
    // 회원가입 요청 데이터 설정
    signupRequest = new UserSignupRequestDto();
    signupRequest.setEmail("test@example.com");
    signupRequest.setPassword("password123");
    signupRequest.setUsername("testuser");
    signupRequest.setLevel("중");
    signupRequest.setGender("남자");
    signupRequest.setBirthDate(new Date());
    signupRequest.setCategoryList(
        Arrays.asList(Category.valueOf("World"), Category.valueOf("US"), Category.valueOf("Sports")));

    // 로그인 요청 데이터 설정
    loginRequest = new UserLoginRequestDto();
    loginRequest.setEmail("test@example.com");
    loginRequest.setPassword("password123");

    // 사용자 엔티티 설정
    user = User.builder()
        .id(1L)
        .email("test@example.com")
        .password("encodedPassword")
        .userName("testuser")
        .gender(Gender.valueOf("남자"))
        .birthDate(new Date())
        .level(Level.valueOf("중"))
        .build();

    // 사용자 관심사 설정
    userInterests = Arrays.asList(
        new UserInterest(1L, "World"),
        new UserInterest(1L, "US"),
        new UserInterest(1L, "Sports")
    );
  }

  @Test
  @DisplayName("회원가입 성공")
  void signupSuccess() {
    // given
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(userInterestRepository.saveAll(anyList())).thenReturn(userInterests);

    // when
    User result = userService.signup(signupRequest);

    // then
    assertNotNull(result);
    assertEquals(user.getEmail(), result.getEmail());
    assertEquals(user.getUserName(), result.getUserName());
    assertEquals(user.getGender(), result.getGender());
    assertEquals(user.getLevel(), result.getLevel());
    verify(userRepository, times(1)).findByEmail(anyString());
    verify(userRepository, times(2)).save(any(User.class));
    verify(userInterestRepository, times(1)).saveAll(anyList());
  }

  @Test
  @DisplayName("회원가입 실패 - 이메일 중복")
  void signupFail_DuplicateEmail() {
    // given
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

    // when & then
    assertThrows(BusinessException.class, () -> userService.signup(signupRequest));
    verify(userRepository, times(1)).findByEmail(anyString());
    verify(userRepository, never()).save(any(User.class));
    verify(userInterestRepository, never()).saveAll(anyList());
  }

  @Test
  @DisplayName("로그인 성공")
  void loginSuccess() {
    // given
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    when(jwtTokenProvider.generateAccessToken(anyString(),anyString())).thenReturn("accessToken");
    when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refreshToken");

    // when
    TokenDto result = userService.login(loginRequest);

    // then
    assertNotNull(result);
    assertEquals("accessToken", result.getAccessToken());
    assertEquals("refreshToken", result.getRefreshToken());
    verify(userRepository, times(1)).findByEmail(anyString());
    verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    verify(jwtTokenProvider, times(1)).generateAccessToken(anyString(),anyString());
    verify(jwtTokenProvider, times(1)).generateRefreshToken(anyString());
  }

  @Test
  @DisplayName("로그인 실패 - 사용자 없음")
  void loginFail_UserNotFound() {
    // given
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    // when & then
    assertThrows(BusinessException.class, () -> userService.login(loginRequest));
    verify(userRepository, times(1)).findByEmail(anyString());
    verify(passwordEncoder, never()).matches(anyString(), anyString());
    verify(jwtTokenProvider, never()).generateAccessToken(anyString(),anyString());
    verify(jwtTokenProvider, never()).generateRefreshToken(anyString());
  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호 불일치")
  void loginFail_InvalidPassword() {
    // given
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

    // when & then
    assertThrows(BusinessException.class, () -> userService.login(loginRequest));
    verify(userRepository, times(1)).findByEmail(anyString());
    verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    verify(jwtTokenProvider, never()).generateAccessToken(anyString(),anyString());
    verify(jwtTokenProvider, never()).generateRefreshToken(anyString());
  }

  @Test
  @DisplayName("이메일로 사용자 찾기 성공")
  void findByEmailSuccess() {
    // given
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

    // when
    User result = userService.findByEmail("test@example.com");

    // then
    assertNotNull(result);
    assertEquals(user.getEmail(), result.getEmail());
    assertEquals(user.getUserName(), result.getUserName());
    assertEquals(user.getGender(), result.getGender());
    assertEquals(user.getLevel(), result.getLevel());
    verify(userRepository, times(1)).findByEmail(anyString());
  }

  @Test
  @DisplayName("이메일로 사용자 찾기 실패")
  void findByEmailFail() {
    // given
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    // when & then
    assertThrows(BusinessException.class, () -> userService.findByEmail("test@example.com"));
    verify(userRepository, times(1)).findByEmail(anyString());
  }

} 
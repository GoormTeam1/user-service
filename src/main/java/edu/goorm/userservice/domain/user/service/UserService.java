package edu.goorm.userservice.domain.user.service;


import edu.goorm.userservice.domain.auth.jwt.JwtTokenProvider;
import edu.goorm.userservice.domain.user.dto.TokenDto;
import edu.goorm.userservice.domain.user.dto.UserLoginRequest;
import edu.goorm.userservice.domain.user.dto.UserSignupRequest;
import edu.goorm.userservice.domain.user.entity.User;
import edu.goorm.userservice.domain.user.repository.UserRepository;
import edu.goorm.userservice.global.exception.BusinessException;
import edu.goorm.userservice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  public User signup(UserSignupRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
    }

    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .username(request.getUsername())
        .role("ROLE_USER")
        .build();

    return userRepository.save(user);
  }

  public TokenDto login(UserLoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BusinessException(ErrorCode.INVALID_PASSWORD);
    }

    return new TokenDto(jwtTokenProvider.generateAccessToken(
        user.getEmail()), jwtTokenProvider.generateRefreshToken(user.getEmail()));
  }

  public User findByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }
}

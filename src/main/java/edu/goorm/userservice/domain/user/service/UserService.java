package edu.goorm.userservice.domain.user.service;


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
import edu.goorm.userservice.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserInterestRepository userInterestRepository;

  public User signup(UserSignupRequestDto request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
    }

    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .username(request.getUsername())
        .level(Level.valueOf(request.getLevel()))
        .gender(Gender.valueOf(request.getGender()))
        .birthDate(request.getBirthDate())
        .role("ROLE_USER")
        .build();

    userRepository.save(user);

    List<UserInterest> interests = request.getCategoryIdList().stream()
        .map(categoryId -> new UserInterest(user.getId(), categoryId))
        .toList();

    userInterestRepository.saveAll(interests);

    return userRepository.save(user);
  }

  public TokenDto login(UserLoginRequestDto request) {
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

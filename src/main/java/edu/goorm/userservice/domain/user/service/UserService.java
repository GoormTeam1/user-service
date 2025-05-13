package edu.goorm.userservice.domain.user.service;


import edu.goorm.userservice.domain.auth.jwt.JwtTokenProvider;
import edu.goorm.userservice.domain.user.dto.CategoryListRequestDto;
import edu.goorm.userservice.domain.user.dto.TokenDto;
import edu.goorm.userservice.domain.user.dto.UserLoginRequestDto;
import edu.goorm.userservice.domain.user.dto.UserSignupRequestDto;
import edu.goorm.userservice.domain.user.entity.Category;
import edu.goorm.userservice.domain.user.entity.Gender;
import edu.goorm.userservice.domain.user.entity.Level;
import edu.goorm.userservice.domain.user.entity.User;
import edu.goorm.userservice.domain.user.entity.UserInterest;
import edu.goorm.userservice.domain.user.entity.UserInterestId;
import edu.goorm.userservice.domain.user.repository.UserInterestRepository;
import edu.goorm.userservice.domain.user.repository.UserRepository;
import edu.goorm.userservice.global.exception.BusinessException;
import edu.goorm.userservice.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
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
  private final KafkaProducerService kafkaProducerService;

  public User signup(UserSignupRequestDto request) {
    if (userRepository.findByUserEmail(request.getEmail()).isPresent()) {
      throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
    }

    User user = User.builder()
        .userEmail((request.getEmail()))
        .password(passwordEncoder.encode(request.getPassword()))
        .userName(request.getUsername())
        .level(Level.valueOf(request.getLevel()))
        .gender(Gender.valueOf(request.getGender()))
        .birthDate(request.getBirthDate())
        .build();

    userRepository.save(user);

    List<UserInterest> interests = request.getCategoryList().stream()
        .map(category -> new UserInterest(user.getUserId(), category))
        .toList();

    userInterestRepository.saveAll(interests);
    kafkaProducerService.sendSignupEvent(user,request.getCategoryList());

    return user;
  }

  public TokenDto login(UserLoginRequestDto request) {
    User user = userRepository.findByUserEmail(request.getEmail())
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BusinessException(ErrorCode.INVALID_PASSWORD);
    }

    return new TokenDto(jwtTokenProvider.generateAccessToken(
        user.getUserEmail(),user.getUserName()), jwtTokenProvider.generateRefreshToken(user.getUserEmail()));
  }

  public User findByEmail(String email) {
    return userRepository.findByUserEmail(email)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }

  @Transactional
  public void updateInterests(String email, CategoryListRequestDto categoryListRequestDto) {
    User user = userRepository.findByUserEmail(email)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    Long userId = user.getUserId();

    userInterestRepository.deleteAllByIdUserId(userId);

    List<Category> categoryList = categoryListRequestDto.getCategoryList();

    for (Category category : categoryList) {
      UserInterestId id = new UserInterestId(userId, category);
      userInterestRepository.save(new UserInterest(id));
    }
  }

  @Transactional
  public void updateLevel(String email, String level) {
    Level levelEnum = Level.valueOf(level); // 문자열과 정확히 일치해야 함

    User user = userRepository.findByUserEmail(email)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    user.setLevel(levelEnum);
  }

  public List<Category> findInterestByUserId(Long id) {
    return userInterestRepository.findCategoriesByUserId(id);
  }
}


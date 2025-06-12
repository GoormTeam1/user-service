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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserInterestRepository userInterestRepository;
  private final KafkaProducerService kafkaProducerService;

//  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider,
//      UserInterestRepository userInterestRepository, KafkaProducerService kafkaProducerService) {
//    this.userRepository = userRepository;
//    this.passwordEncoder = passwordEncoder;
//    this.jwtTokenProvider = jwtTokenProvider;
//    this.userInterestRepository = userInterestRepository;
//    try {
//      this.kafkaProducerService = kafkaProducerService;
//    } catch (Exception e) {
//      kafkaProducerService = null;
//      log.warn("Kafka 전송 실패: {}", e.getMessage());
//    }
//  }

  @Transactional
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

    // ✅ Kafka는 예외를 삼켜서 DB 트랜잭션에 영향 주지 않도록
    try {
      kafkaProducerService.sendSignupEvent(user, request.getCategoryList());
    } catch (Exception e) {
      log.warn("Kafka 전송 실패. 사용자 ID: {}, 사유: {}", user.getUserId(), e.getMessage());
      // 필요 시 DB에 실패 로그 저장 or Dead Letter Queue로 재처리
    }

    userInterestRepository.saveAll(interests);

    return user;
  }

  public TokenDto login(UserLoginRequestDto request) {
    User user = userRepository.findByUserEmail(request.getEmail())
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BusinessException(ErrorCode.INVALID_PASSWORD);
    }

    return new TokenDto(jwtTokenProvider.generateAccessToken(
        user.getUserEmail(), user.getUserName()), jwtTokenProvider.generateRefreshToken(user.getUserEmail()));
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

    // 기존 관심 카테고리 삭제
    userInterestRepository.deleteAllByIdUserId(userId);
    userInterestRepository.flush();


    List<Category> categoryList = categoryListRequestDto.getCategoryList();


    // ✅ Kafka는 예외를 삼켜서 DB 트랜잭션에 영향 주지 않도록
    try {
      kafkaProducerService.sendUpdateInterestEvent(user, categoryList);
    } catch (Exception e) {
      log.warn("Kafka 전송 실패. 사용자 ID: {}, 사유: {}", user.getUserId(), e.getMessage());
      // 필요 시 DB에 실패 로그 저장 or Dead Letter Queue로 재처리
    }


    // 벌크 insert할 UserInterest 리스트 생성
    List<UserInterest> interests = categoryList.stream()
        .map(category -> new UserInterest(new UserInterestId(userId, category)))
        .toList();

    userInterestRepository.saveAll(interests);  // 벌크 저장
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


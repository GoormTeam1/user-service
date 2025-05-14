package edu.goorm.userservice.domain.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.goorm.userservice.domain.user.entity.Category;
import edu.goorm.userservice.domain.user.entity.Gender;
import edu.goorm.userservice.domain.user.entity.Level;
import edu.goorm.userservice.domain.user.entity.User;
import java.text.SimpleDateFormat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public void sendSignupEvent(User user, List<Category> categoryList) {
    try {
      String json = objectMapper.writeValueAsString(userToMap(user,categoryList));
      kafkaTemplate.send("user.signup", json);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Kafka 메시지 직렬화 실패", e);
    }
  }

  public void sendUpdateInterestEvent(User user, List<Category> categoryList) {
    try {
      String json = objectMapper.writeValueAsString(userToMap(user,categoryList));
      kafkaTemplate.send("user.updateInterest", json);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Kafka 메시지 직렬화 실패", e);
    }
  }

  private static SignupPayload userToMap(User user, List<Category> categoryList) {
    return new SignupPayload(
        user.getUserId(),
        user.getUserEmail(),
        user.getGender(),
        user.getLevel(),
        new SimpleDateFormat("yyyy-MM-dd").format(user.getBirthDate()),
        categoryList
    );
  }

  // 내부 클래스 or 별도 DTO
  public record SignupPayload(
      Long userId,
      String email,
      Gender gender,
      Level level,
      String birthDate,
      List<Category> interests
  ) {}
}

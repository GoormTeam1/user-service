package edu.goorm.userservice.domain.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.goorm.userservice.domain.user.entity.Category;
import edu.goorm.userservice.domain.user.entity.Gender;
import edu.goorm.userservice.domain.user.entity.Level;
import edu.goorm.userservice.domain.user.entity.User;
import edu.goorm.userservice.global.logger.CustomLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public void sendSignupEvent(User user, List<Category> categoryList) {
    sendKafkaEvent("user.signup", user, categoryList);
  }

  public void sendUpdateInterestEvent(User user, List<Category> categoryList) {
    sendKafkaEvent("user.updateInterest", user, categoryList);
  }

  private void sendKafkaEvent(String topic, User user, List<Category> categoryList) {
    String key = String.valueOf(user.getUserId());
    Map<String, String> contextMap = MDC.getCopyOfContextMap(); // ✅ MDC context 저장

    try {
      String json = objectMapper.writeValueAsString(userToMap(user, categoryList));

      CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, json);

      future.whenComplete((result, ex) -> {
        if (contextMap != null) MDC.setContextMap(contextMap); // ✅ context 복원

        if (ex == null) {
          log.info("KAFKA_SEND_SUCCESS topic={} userId={} offset={}", topic, key, result.getRecordMetadata().offset());
          CustomLogger.logExternalKafkaSend(topic, key, "SUCCESS");
        } else {
          log.error("KAFKA_SEND_FAILURE topic={} userId={} error={}", topic, key, ex.getMessage(), ex);
          CustomLogger.logExternalKafkaSend(topic, key, "FAILURE");
        }

        MDC.clear(); // ✅ 비동기 쓰레드 cleanup
      });

    } catch (JsonProcessingException e) {
      log.error("KAFKA_SERIALIZATION_FAILED topic={} userId={} error={}", topic, key, e.getMessage(), e);
      CustomLogger.logExternalKafkaSend(topic, key, "FAILURE");
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

  public record SignupPayload(
      Long userId,
      String email,
      Gender gender,
      Level level,
      String birthDate,
      List<Category> interests
  ) {}
}

package edu.goorm.userservice.domain.user.entity;

import edu.goorm.userservice.domain.user.entity.UserInterestId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_interest")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInterest {

  @EmbeddedId
  private UserInterestId id;

  public UserInterest(Long userId, Long categoryId) {
    this.id = new UserInterestId(userId, categoryId);
  }
}

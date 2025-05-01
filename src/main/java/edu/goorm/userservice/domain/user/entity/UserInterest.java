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

  public UserInterest(Long userId, Category category) {
    this.id = new UserInterestId(userId, category);
  }
  public UserInterest(Long userId, String category) {
    this.id = new UserInterestId(userId, Category.valueOf(category));
  }
}

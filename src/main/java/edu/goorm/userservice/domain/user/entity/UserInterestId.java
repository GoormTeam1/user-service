package edu.goorm.userservice.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInterestId implements Serializable {

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "category_id")
  private Long categoryId;
}

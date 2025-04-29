package edu.goorm.userservice.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccessTokenDto {
  private String accessToken;
}

package edu.goorm.userservice.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfoResponse {
  private Long id;
  private String email;
  private String username;
}

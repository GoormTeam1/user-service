package edu.goorm.userservice.domain.user.dto;

import lombok.Data;

@Data
public class UserSignupRequest {
  private String email;
  private String password;
  private String username;
}

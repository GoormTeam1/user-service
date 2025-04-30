package edu.goorm.userservice.domain.user.dto;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class UserSignupRequestDto {
  private String email;
  private String password;
  private String username;
  private String level;
  private Date birthDate;
  private String gender;
  private List<Long> categoryIdList;
}

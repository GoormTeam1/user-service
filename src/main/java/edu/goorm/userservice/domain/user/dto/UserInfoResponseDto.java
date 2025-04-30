package edu.goorm.userservice.domain.user.dto;

import edu.goorm.userservice.domain.user.entity.Gender;
import edu.goorm.userservice.domain.user.entity.Level;
import edu.goorm.userservice.domain.user.entity.User;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfoResponseDto {
  private Long id;
  private String email;
  private String password;
  private String username;
  private Level level;
  private Date birthDate;
  private Gender gender;
  private List<Long> categoryId;

  public UserInfoResponseDto(User user){
    this.id = user.getId();
    this.email = user.getEmail();
    this.username = user.getUsername();
    this.level = user.getLevel();
    this.birthDate = user.getBirthDate();
    this.gender = user.getGender();
  }
}

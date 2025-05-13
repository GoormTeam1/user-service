package edu.goorm.userservice.domain.user.dto;

import edu.goorm.userservice.domain.user.entity.Category;
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
  private String userName;
  private Level level;
  private Date birthDate;
  private Gender gender;
  private List<Category> categoryList;

  public UserInfoResponseDto(User user, List<Category> categoryList){
    this.id = user.getUserId();
    this.email = user.getUserEmail();
    this.userName = user.getUserName();
    this.level = user.getLevel();
    this.birthDate = user.getBirthDate();
    this.gender = user.getGender();
    this.categoryList = categoryList;
  }
}

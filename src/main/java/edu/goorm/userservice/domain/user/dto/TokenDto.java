package edu.goorm.userservice.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TokenDto {
  String AccessToken;
  String RefreshToken;

}

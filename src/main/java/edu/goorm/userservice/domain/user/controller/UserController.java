package edu.goorm.userservice.domain.user.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.goorm.userservice.domain.user.dto.AccessTokenDto;
import edu.goorm.userservice.domain.user.dto.TokenDto;
import edu.goorm.userservice.domain.user.dto.UserInfoResponseDto;
import edu.goorm.userservice.domain.user.dto.UserLoginRequestDto;
import edu.goorm.userservice.domain.user.dto.UserSignupRequestDto;
import edu.goorm.userservice.domain.user.entity.User;
import edu.goorm.userservice.domain.user.service.UserService;
import edu.goorm.userservice.global.exception.BusinessException;
import edu.goorm.userservice.global.exception.ErrorCode;
import edu.goorm.userservice.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/signup")
  public ResponseEntity<?> signup(@RequestBody UserSignupRequestDto request) {
    UserInfoResponseDto userInfoResponseDto = new UserInfoResponseDto(userService.signup(request));
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.CREATED, "회원가입 성공", userInfoResponseDto));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequestDto request,
      HttpServletResponse response) {
    TokenDto tokenDto = userService.login(request);
    AccessTokenDto accessTokenDto = new AccessTokenDto(tokenDto.getAccessToken());

    // HttpOnly 쿠키로 설정
    ResponseCookie cookie = ResponseCookie.from("token", tokenDto.getRefreshToken())
        .httpOnly(true)
        .path("/")
        .maxAge(86400) // 1 day
        .sameSite("Strict")
        .secure(false) // prod에서는 true
        .build();

    response.addHeader("Set-Cookie", cookie.toString());
    System.out.println(tokenDto.getAccessToken());
    System.out.println(tokenDto.getRefreshToken());

    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "로그인 성공", accessTokenDto));
  }

  @GetMapping("/me")
  public ResponseEntity<?> getMyInfo(@RequestHeader("X-User-Email") String email) {
    if (email == null || email.isBlank()) {
      throw new BusinessException(ErrorCode.NO_AUTH_HEADER);
    }

    User user = userService.findByEmail(email);

    UserInfoResponseDto response = new UserInfoResponseDto(user);

    return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, "회원 정보 불러오기 성공", response));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletResponse response) {
    // 쿠키 만료: maxAge = 0
    ResponseCookie cookie = ResponseCookie.from("token", "")
        .httpOnly(true)
        .path("/")
        .maxAge(0)
        .sameSite("Strict")
        .secure(false) // 배포 시 true
        .build();

    response.addHeader("Set-Cookie", cookie.toString());

    return ResponseEntity.ok("로그아웃 성공");
  }
}

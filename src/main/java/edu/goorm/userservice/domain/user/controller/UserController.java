package edu.goorm.userservice.domain.user.controller;


import edu.goorm.userservice.domain.user.dto.CategoryListRequestDto;
import edu.goorm.userservice.domain.user.dto.LevelRequestDto;
import edu.goorm.userservice.domain.user.entity.Category;
import edu.goorm.userservice.domain.user.entity.Level;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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
  public ResponseEntity<?> signup(@Valid @RequestBody UserSignupRequestDto request) {
    userService.signup(request);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.CREATED, "회원가입 성공", null));
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

    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "로그인 성공", accessTokenDto));
  }

  @GetMapping("/me")
  public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
    String email = userDetails.getUsername();
    System.out.println(email);
    if (email == null || email.isBlank()) {
      throw new BusinessException(ErrorCode.NO_AUTH_HEADER);
    }

    User user = userService.findByEmail(email);
    List<Category> categoryList = userService.findInterestByUserId(user.getId());

    UserInfoResponseDto response = new UserInfoResponseDto(user, categoryList);
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

  @PutMapping("/interests")
  public ResponseEntity<?> updateInterests(@AuthenticationPrincipal UserDetails userDetails,
      @RequestBody
      CategoryListRequestDto categoryListRequestDto) {

    String email = userDetails.getUsername();
    System.out.println("email = " + email);
    userService.updateInterests(email, categoryListRequestDto);

    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "관심 카테고리 변경 성공", null));
  }

  @PatchMapping("/level")
  public ResponseEntity<?> updateLevel(@AuthenticationPrincipal UserDetails userDetails,
      @RequestBody
      LevelRequestDto levelRequestDto) {
    String email = userDetails.getUsername();
    userService.updateLevel(email, levelRequestDto.getLevel());

    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "난이도 변경 성공", null));
  }
}

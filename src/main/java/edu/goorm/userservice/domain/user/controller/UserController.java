package edu.goorm.userservice.domain.user.controller;


import edu.goorm.userservice.domain.user.dto.CategoryListRequestDto;
import edu.goorm.userservice.domain.user.dto.LevelRequestDto;
import edu.goorm.userservice.domain.user.entity.Category;
import edu.goorm.userservice.domain.user.entity.Level;
import edu.goorm.userservice.global.logger.CustomLogger;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
  public ResponseEntity<?> signup(@Valid @RequestBody UserSignupRequestDto userSignupRequestDto,
      HttpServletRequest request) {
    userService.signup(userSignupRequestDto);

    CustomLogger.logRequest(
        "USER_SIGNUP",
        "/api/user/signup",
        "POST",
        null,
        String.format("{\"userEmail\": \"%s\"}", userSignupRequestDto.getEmail()),
        request
    );

    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.CREATED, "회원가입 성공", null));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequestDto requestDto,
      HttpServletResponse response,
      HttpServletRequest request) {
    TokenDto tokenDto = userService.login(requestDto);
    AccessTokenDto accessTokenDto = new AccessTokenDto(tokenDto.getAccessToken());

    ResponseCookie cookie = ResponseCookie.from("token", tokenDto.getRefreshToken())
        .httpOnly(true)
        .path("/")
        .maxAge(86400)
        .sameSite("Strict")
        .secure(false)
        .build();

    response.addHeader("Set-Cookie", cookie.toString());

    CustomLogger.logRequest(
        "USER_LOGIN",
        "/api/user/login",
        "POST",
        null,
        String.format("{\"userEmail\": \"%s\"}", requestDto.getEmail()),
        request
    );

    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "로그인 성공", accessTokenDto));
  }

  @GetMapping("/me")
  public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal UserDetails userDetails,
      HttpServletRequest request) {
    String email = userDetails.getUsername();
    if (email == null || email.isBlank()) {
      throw new BusinessException(ErrorCode.NO_AUTH_HEADER);
    }

    User user = userService.findByEmail(email);
    List<Category> categoryList = userService.findInterestByUserId(user.getUserId());

    CustomLogger.logRequest(
        "FETCH_USER_INFO",
        "/api/user/me",
        "GET",
        email,
        null,
        request
    );

    UserInfoResponseDto response = new UserInfoResponseDto(user, categoryList);
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, "회원 정보 불러오기 성공", response));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletResponse response, HttpServletRequest request) {
    ResponseCookie cookie = ResponseCookie.from("token", "")
        .httpOnly(true)
        .path("/")
        .maxAge(0)
        .sameSite("Strict")
        .secure(false)
        .build();

    response.addHeader("Set-Cookie", cookie.toString());

    CustomLogger.logRequest(
        "USER_LOGOUT",
        "/api/user/logout",
        "POST",
        null,
        null,
        request
    );

    return ResponseEntity.ok("로그아웃 성공");
  }

  @PutMapping("/interests")
  public ResponseEntity<?> updateInterests(@AuthenticationPrincipal UserDetails userDetails,
      @RequestBody CategoryListRequestDto categoryListRequestDto,
      HttpServletRequest request) {
    String email = userDetails.getUsername();
    userService.updateInterests(email, categoryListRequestDto);

    CustomLogger.logRequest(
        "UPDATE_INTERESTS",
        "/api/user/interests",
        "PUT",
        email,
        categoryListRequestDto.toString(),
        request
    );

    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "관심 카테고리 변경 성공", null));
  }

  @PatchMapping("/level")
  public ResponseEntity<?> updateLevel(@AuthenticationPrincipal UserDetails userDetails,
      @RequestBody LevelRequestDto levelRequestDto,
      HttpServletRequest request) {
    String email = userDetails.getUsername();
    userService.updateLevel(email, levelRequestDto.getLevel());

    CustomLogger.logRequest(
        "UPDATE_LEVEL",
        "/api/user/level",
        "PATCH",
        email,
        String.format("{\"newLevel\": \"%s\"}", levelRequestDto.getLevel()),
        request
    );

    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "난이도 변경 성공", null));
  }

  @GetMapping("/internal/find-id-by-email")
  Long getUserIdByEmail(@RequestHeader("X-USER-EMAIL") String email, HttpServletRequest request) {
    CustomLogger.logRequest(
        "FIND_USER_ID",
        "/api/user/internal/find-id-by-email",
        "GET",
        email,
        null,
        request
    );
    return userService.findByEmail(email).getUserId();
  }

}

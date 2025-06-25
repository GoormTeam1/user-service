package edu.goorm.userservice.domain.user.controller;

import edu.goorm.userservice.domain.user.dto.*;
import edu.goorm.userservice.domain.user.entity.Category;
import edu.goorm.userservice.domain.user.entity.User;
import edu.goorm.userservice.domain.user.service.UserService;
import edu.goorm.userservice.global.exception.BusinessException;
import edu.goorm.userservice.global.exception.ErrorCode;
import edu.goorm.userservice.global.logger.CustomLogger;
import edu.goorm.userservice.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/signup")
  public ResponseEntity<?> signup(@Valid @RequestBody UserSignupRequestDto userSignupRequestDto,
      HttpServletRequest request) {
    long startTime = System.currentTimeMillis();
    userService.signup(userSignupRequestDto);
    long endTime = System.currentTimeMillis();

    CustomLogger.logRequest(
        "USER_SIGNUP",
        request.getRequestURI(),
        "POST",
        userSignupRequestDto.getEmail(),
        request,
        HttpStatus.CREATED.value(),
        endTime - startTime
    );

    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.CREATED, "회원가입 성공!", null));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequestDto requestDto,
      HttpServletResponse response,
      HttpServletRequest request) {
    long startTime = System.currentTimeMillis();

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
    long endTime = System.currentTimeMillis();

    CustomLogger.logRequest(
        "USER_LOGIN",
        request.getRequestURI(),
        "POST",
        requestDto.getEmail(),
        request,
        HttpStatus.OK.value(),
        endTime - startTime
    );

    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "로그인 성공", accessTokenDto));
  }

  @GetMapping("/me")
  public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal UserDetails userDetails,
      HttpServletRequest request) {
    long startTime = System.currentTimeMillis();

    String email = userDetails.getUsername();
    if (email == null || email.isBlank()) {
      throw new BusinessException(ErrorCode.NO_AUTH_HEADER);
    }

    User user = userService.findByEmail(email);
    List<Category> categoryList = userService.findInterestByUserId(user.getUserId());

    long endTime = System.currentTimeMillis();
    CustomLogger.logRequest(
        "FETCH_USER_INFO",
        request.getRequestURI(),
        "GET",
        email,
        request,
        HttpStatus.OK.value(),
        endTime - startTime
    );

    UserInfoResponseDto response = new UserInfoResponseDto(user, categoryList);
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "회원 정보 불러오기 성공", response));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletResponse response, HttpServletRequest request) {
    long startTime = System.currentTimeMillis();

    ResponseCookie cookie = ResponseCookie.from("token", "")
        .httpOnly(true)
        .path("/")
        .maxAge(0)
        .sameSite("Strict")
        .secure(false)
        .build();

    response.addHeader("Set-Cookie", cookie.toString());
    long endTime = System.currentTimeMillis();

    CustomLogger.logRequest(
        "USER_LOGOUT",
        request.getRequestURI(),
        "POST",
        null,
        request,
        HttpStatus.OK.value(),
        endTime - startTime
    );

    return ResponseEntity.ok("로그아웃 성공");
  }

  @PutMapping("/interests")
  public ResponseEntity<?> updateInterests(@AuthenticationPrincipal UserDetails userDetails,
      @RequestBody CategoryListRequestDto categoryListRequestDto,
      HttpServletRequest request) {
    long startTime = System.currentTimeMillis();

    String email = userDetails.getUsername();
    userService.updateInterests(email, categoryListRequestDto);

    long endTime = System.currentTimeMillis();
    CustomLogger.logRequest(
        "UPDATE_INTERESTS",
        request.getRequestURI(),
        "PUT",
        categoryListRequestDto.toString(),
        request,
        HttpStatus.OK.value(),
        endTime - startTime
    );

    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "관심 카테고리 변경 성공", null));
  }

  @PatchMapping("/level")
  public ResponseEntity<?> updateLevel(@AuthenticationPrincipal UserDetails userDetails,
      @RequestBody LevelRequestDto levelRequestDto,
      HttpServletRequest request) {
    long startTime = System.currentTimeMillis();

    String email = userDetails.getUsername();
    userService.updateLevel(email, levelRequestDto.getLevel());

    long endTime = System.currentTimeMillis();
    CustomLogger.logRequest(
        "UPDATE_LEVEL",
        request.getRequestURI(),
        "PATCH",
        email,
        request,
        HttpStatus.OK.value(),
        endTime - startTime
    );

    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "난이도 변경 성공", null));
  }

  @GetMapping("/internal/find-id-by-email")
  public Long getUserIdByEmail(@RequestHeader("X-USER-EMAIL") String email, HttpServletRequest request) {
    long startTime = System.currentTimeMillis();

    Long userId = userService.findByEmail(email).getUserId();

    long endTime = System.currentTimeMillis();
    CustomLogger.logRequest(
        "FIND_USER_ID",
        request.getRequestURI(),
        "GET",
        email,
        request,
        HttpStatus.OK.value(),
        endTime - startTime
    );

    return userId;
  }
}

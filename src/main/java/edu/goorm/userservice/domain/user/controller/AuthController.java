package edu.goorm.userservice.domain.user.controller;

import edu.goorm.userservice.domain.auth.jwt.JwtUserDetailsService;
import edu.goorm.userservice.domain.auth.jwt.JwtTokenProvider;
import edu.goorm.userservice.domain.user.dto.AccessTokenDto;
import edu.goorm.userservice.global.exception.BusinessException;
import edu.goorm.userservice.global.exception.ErrorCode;
import edu.goorm.userservice.global.response.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtUserDetailsService userDetailsService;

  @PostMapping("/reissue")
  public ResponseEntity<ApiResponse<AccessTokenDto>> reissueToken(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = extractTokenFromCookie(request);

    if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
      throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }

    String email = jwtTokenProvider.getEmail(refreshToken);

    var userDetails = userDetailsService.loadUserByUsername(email);

    // 새 Access Token 발급
    String newAccessToken = jwtTokenProvider.generateAccessToken(email);

    // (선택) 새 Refresh Token도 발급
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

    // 새 Refresh Token을 쿠키에 저장
    Cookie newRefreshCookie = new Cookie("token", newRefreshToken);
    newRefreshCookie.setHttpOnly(true);
    newRefreshCookie.setPath("/");
    newRefreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
    response.addCookie(newRefreshCookie);

    // 새 Access Token을 응답
    AccessTokenDto accessTokenDto = new AccessTokenDto(newAccessToken);

    return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED,"토큰 재발급 성공", accessTokenDto));
  }

  private String extractTokenFromCookie(HttpServletRequest request) {
    if (request.getCookies() == null) return null;
    for (Cookie cookie : request.getCookies()) {
      if ("token".equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}

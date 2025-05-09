package edu.goorm.userservice.domain.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.accessToken.expiration}")
  private long accessTokenExpirationTime;

  @Value("${jwt.refreshToken.expiration}")
  private long refreshTokenExpirationTime;

  private Key key;

  @PostConstruct
  protected void init() {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
  }

  // Access Token 생성
  public String generateAccessToken(String email, String username) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + accessTokenExpirationTime); // 예: 15분

    return Jwts.builder()
        .setSubject(email)
        .claim("username",username)
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  // Refresh Token 생성
  public String generateRefreshToken(String email) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + refreshTokenExpirationTime); // 예: 7일

    return Jwts.builder()
        .setSubject(email)
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }


  public String getEmail(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build()
        .parseClaimsJws(token).getBody().getSubject();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build()
          .parseClaimsJws(token);
      return true;
    } catch (JwtException e) {
      return false;
    }
  }
}

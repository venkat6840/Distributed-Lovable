package com.coding.distributed_lovable.common_lib.security;

import com.coding.distributed_lovable.common_lib.enums.ProjectRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

  @Value("${jwt.secretKey}")
  private String secretKey;

  private SecretKey getKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(JwtUserPrincipal user) {
    return Jwts.builder()
        .subject(user.userName())
        .claim("userId", user.userId().toString())
        .claim("email", user.getUsername())
        .claim("name", user.name())
        .claim("roles", ProjectRole.values())
        .signWith(getKey())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 100))
        .compact();
  }

  public JwtUserPrincipal parseAccessToken(String token) {
    Claims claims =
        Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
    String name = claims.get("name", String.class);
    return new JwtUserPrincipal(
        Long.parseLong(claims.get("userId", String.class)),
        claims.getSubject(),
        name,
        null,
        new ArrayList<>());
  }

  public Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal)) {
      throw new AuthenticationCredentialsNotFoundException("No JWT found");
    }
    return ((JwtUserPrincipal) authentication.getPrincipal()).userId();
  }
}

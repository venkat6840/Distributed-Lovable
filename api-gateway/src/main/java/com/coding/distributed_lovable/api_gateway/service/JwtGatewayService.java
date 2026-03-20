package com.coding.distributed_lovable.api_gateway.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtGatewayService {

  @Value("${jwt.secretKey}")
  private String jwtSecretKey;

  public void validateToken(String token) {
    SecretKey key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
  }
}

package com.coding.distributed_lovable.account_service.controller;

import com.coding.distributed_lovable.account_service.dto.Auth.AuthResponse;
import com.coding.distributed_lovable.account_service.dto.Auth.LoginRequest;
import com.coding.distributed_lovable.account_service.dto.Auth.SignupRequest;
import com.coding.distributed_lovable.account_service.service.AuthService;
import com.coding.distributed_lovable.account_service.service.impl.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @Author - Venkatesh G
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthController {

  AuthService authService;
  UserService userService;

  @PostMapping("/signup")
  public ResponseEntity<AuthResponse> signUp(@RequestBody @Valid SignupRequest request) {
    return ResponseEntity.ok(authService.signup(request));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> signUp(@RequestBody @Valid LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }
}

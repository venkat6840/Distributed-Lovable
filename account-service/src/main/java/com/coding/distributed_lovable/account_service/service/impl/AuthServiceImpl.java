package com.coding.distributed_lovable.account_service.service.impl;

import com.coding.distributed_lovable.account_service.dto.Auth.AuthResponse;
import com.coding.distributed_lovable.account_service.dto.Auth.LoginRequest;
import com.coding.distributed_lovable.account_service.dto.Auth.SignupRequest;
import com.coding.distributed_lovable.account_service.entity.User;
import com.coding.distributed_lovable.account_service.mapper.UserMapper;
import com.coding.distributed_lovable.account_service.repository.UserRepository;
import com.coding.distributed_lovable.account_service.service.AuthService;
import com.coding.distributed_lovable.common_lib.error.BadRequestException;
import com.coding.distributed_lovable.common_lib.security.AuthUtil;
import com.coding.distributed_lovable.common_lib.security.JwtUserPrincipal;
import java.util.ArrayList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

  UserRepository userRepository;
  UserMapper userMapper;
  PasswordEncoder passwordEncoder;
  AuthUtil authUtil;
  AuthenticationManager authenticationManager;

  @Override
  public AuthResponse signup(SignupRequest request) {
    userRepository
        .findByUsername(request.username())
        .ifPresent(
            user -> {
              throw new BadRequestException(
                  "User already exists with user name " + request.username());
            });
    User user = userMapper.toEntity(request);
    user.setPassword(passwordEncoder.encode(request.password()));
    user = userRepository.save(user);

    JwtUserPrincipal jwtUserPrincipal =
        new JwtUserPrincipal(
            user.getId(), user.getUsername(), user.getName(), null, new ArrayList<>());
    String token = authUtil.generateAccessToken(jwtUserPrincipal);
    return new AuthResponse(token, userMapper.toUserProfileResponse(jwtUserPrincipal));
  }

  @Override
  public AuthResponse login(LoginRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));

    JwtUserPrincipal user = (JwtUserPrincipal) authentication.getPrincipal();
    String token = authUtil.generateAccessToken(user);

    return new AuthResponse(token, userMapper.toUserProfileResponse(user));
  }
}

package com.coding.distributed_lovable.account_service.service;

import com.coding.distributed_lovable.account_service.dto.Auth.AuthResponse;
import com.coding.distributed_lovable.account_service.dto.Auth.LoginRequest;
import com.coding.distributed_lovable.account_service.dto.Auth.SignupRequest;
/**
 * @Author - Venkatesh G
 */
public interface AuthService {

  AuthResponse signup(SignupRequest request);

  AuthResponse login(LoginRequest request);
}

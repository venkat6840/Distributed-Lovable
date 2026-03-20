package com.coding.distributed_lovable.account_service.service.impl;

import com.coding.distributed_lovable.account_service.entity.User;
import com.coding.distributed_lovable.account_service.repository.UserRepository;
import com.coding.distributed_lovable.common_lib.error.ResourceNotFoundException;
import com.coding.distributed_lovable.common_lib.security.JwtUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("user not found", username));

    return new JwtUserPrincipal(
        user.getId(), user.getUsername(), user.getName(), user.getPassword(), new ArrayList<>());
  }
}

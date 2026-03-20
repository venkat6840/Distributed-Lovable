package com.coding.distributed_lovable.account_service.controller;

import com.coding.distributed_lovable.account_service.mapper.UserMapper;
import com.coding.distributed_lovable.account_service.repository.UserRepository;
import com.coding.distributed_lovable.account_service.service.SubscriptionService;
import com.coding.distributed_lovable.common_lib.dto.PlanDto;
import com.coding.distributed_lovable.common_lib.dto.UserDto;
import com.coding.distributed_lovable.common_lib.error.ResourceNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/v1")
@RequiredArgsConstructor
public class InternalAccountController {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final SubscriptionService subscriptionService;

  @GetMapping("/users/{id}")
  public UserDto getUserById(@PathVariable Long id) {
    return userRepository
        .findById(id)
        .map(userMapper::toUser)
        .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
  }

  @GetMapping("/users/by-email")
  public Optional<UserDto> getUserByEmail(@RequestParam String email) {
    return userRepository.findByUsernameIgnoreCase(email).map(userMapper::toUser);
  }

  @GetMapping("/billing/current-plan")
  public PlanDto getCurrentSubscribedPlan() {
    return subscriptionService.getCurrentSubscribedPlanByUser();
  }
}

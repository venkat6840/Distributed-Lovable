package com.coding.distributed_lovable.Intelligent_service.client;

import com.coding.distributed_lovable.common_lib.dto.PlanDto;
import com.coding.distributed_lovable.common_lib.dto.UserDto;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "account-service", path = "/account", url = "${ACCOUNT_SERVICE_URI:}")
public interface AccountClient {

  @GetMapping("/internal/v1/users/by-email")
  Optional<UserDto> getUserByEmail(@RequestParam("email") String email);

  @GetMapping("/internal/v1/billing/current-plan")
  PlanDto getCurrentSubscribedPlanByUser();
}

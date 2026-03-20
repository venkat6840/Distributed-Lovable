package com.coding.distributed_lovable.Intelligent_service.service.impl;

import com.coding.distributed_lovable.Intelligent_service.client.AccountClient;
import com.coding.distributed_lovable.Intelligent_service.entity.UsageLog;
import com.coding.distributed_lovable.Intelligent_service.repository.UsageLogRepository;
import com.coding.distributed_lovable.Intelligent_service.service.UsageService;
import com.coding.distributed_lovable.common_lib.dto.PlanDto;
import com.coding.distributed_lovable.common_lib.security.AuthUtil;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UsageServiceImpl implements UsageService {

  private final UsageLogRepository usageLogRepository;
  private final AuthUtil authUtil;
  private final AccountClient accountClient;

  @Override
  public void recordTokenUsage(Long userId, int actualTokens) {
    LocalDate today = LocalDate.now();
    UsageLog todayLog =
        usageLogRepository
            .findAllByUserIdAndDate(userId, today)
            .orElseGet(() -> createNewDailyLog(userId, today));
    todayLog.setTokensUsed(todayLog.getTokensUsed() + actualTokens);
    usageLogRepository.save(todayLog);
  }

  @Override
  public void checkDailyTokenUsage() {
    Long userId = authUtil.getCurrentUserId();
    PlanDto planResponse = accountClient.getCurrentSubscribedPlanByUser();
    LocalDate today = LocalDate.now();
    UsageLog todayLog =
        usageLogRepository
            .findAllByUserIdAndDate(userId, today)
            .orElseGet(() -> createNewDailyLog(userId, today));

    if (planResponse.unlimitedAi()) return;

    int currentUsage = todayLog.getTokensUsed();
    int limit = planResponse.maxTokensPerDay();

    if (currentUsage >= limit) {
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS, "Daily limit reached, upgrade now");
    }
  }

  private UsageLog createNewDailyLog(Long userId, LocalDate date) {
    UsageLog log = UsageLog.builder().userId(userId).date(date).tokensUsed(0).build();
    return log;
  }
}

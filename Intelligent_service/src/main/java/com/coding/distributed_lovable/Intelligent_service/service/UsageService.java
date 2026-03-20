package com.coding.distributed_lovable.Intelligent_service.service;

public interface UsageService {

  void recordTokenUsage(Long userId, int actualTokens);

  public void checkDailyTokenUsage();
}

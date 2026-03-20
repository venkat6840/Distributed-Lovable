package com.coding.distributed_lovable.account_service.dto.subscription;

public record PlanLimitResponse(
    String planName, Integer maxTokensPerDay, Integer maxProjects, Boolean unlimitedAi) {}

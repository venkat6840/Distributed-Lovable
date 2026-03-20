package com.coding.distributed_lovable.workspace_service.dto.project;

import com.coding.distributed_lovable.workspace_service.dto.account.UserProfileResponse;

import java.time.Instant;

public record ProjectResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        UserProfileResponse profileResponse) {
}

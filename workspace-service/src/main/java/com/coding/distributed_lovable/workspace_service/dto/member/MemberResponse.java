package com.coding.distributed_lovable.workspace_service.dto.member;

import com.coding.distributed_lovable.common_lib.enums.ProjectRole;

import java.time.Instant;

public record MemberResponse(
        Long userId, String username, String name, ProjectRole projectRole, Instant invitedAt) {}

package com.coding.distributed_lovable.workspace_service.dto.member;

import com.coding.distributed_lovable.common_lib.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(@NotNull ProjectRole role) {}

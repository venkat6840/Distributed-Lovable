package com.coding.distributed_lovable.workspace_service.security;

import com.coding.distributed_lovable.common_lib.enums.Permission;
import com.coding.distributed_lovable.common_lib.security.AuthUtil;
import com.coding.distributed_lovable.workspace_service.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @Author - Venkatesh G
 */
@Component("security")
@RequiredArgsConstructor
public class SecurityExpressions {

  private final ProjectMemberRepository projectMemberRepository;
  private final AuthUtil authUtil;

  public boolean canViewProject(Long projectId) {
    return hasPermission(projectId, Permission.VIEW);
  }

  public boolean canEditProject(Long projectId) {
    return hasPermission(projectId, Permission.EDIT);
  }

  public boolean canDeleteProject(Long projectId) {
    return hasPermission(projectId, Permission.DELETE);
  }

  public boolean canViewMembers(Long projectId) {
    return hasPermission(projectId, Permission.VIEW_MEMBERS);
  }

  public boolean canManageMembers(Long projectId) {
    return hasPermission(projectId, Permission.MANAGE_MEMBERS);
  }

  public boolean hasPermission(Long projectId, Permission projectPermission) {
    Long userId = authUtil.getCurrentUserId();

    return projectMemberRepository
        .findRoleByProjectIdAndUserId(projectId, userId)
        .map(role -> role.getProjectPermissions().contains(projectPermission))
        .orElse(false);
  }
}

package com.coding.distributed_lovable.Intelligent_service.security;

import com.coding.distributed_lovable.Intelligent_service.client.WorkspaceClient;
import com.coding.distributed_lovable.common_lib.enums.Permission;
import com.coding.distributed_lovable.common_lib.security.AuthUtil;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Component;

/**
 * @Author - Venkatesh G
 */
@Component("security")
@RequiredArgsConstructor
@Slf4j
public class SecurityExpressions {

  private final AuthUtil authUtil;
  private final WorkspaceClient workspaceClient;

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

  private boolean hasPermission(Long projectId, Permission projectPermission) {
    try {
      return workspaceClient.checkPermission(projectId, projectPermission);
    } catch (FeignException.Unauthorized e) {
      log.warn("Token expired or invalid during permission check for project: {}", projectId);
      throw new CredentialsExpiredException("JWT token is expired or invalid");
    } catch (FeignException e) {
      log.error("Workspace-service failed during permission check: {}", e.getMessage());
      return false;
    }
  }
}

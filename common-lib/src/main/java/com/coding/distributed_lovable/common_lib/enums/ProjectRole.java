package com.coding.distributed_lovable.common_lib.enums;

import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import static com.coding.distributed_lovable.common_lib.enums.Permission.*;

@RequiredArgsConstructor
@Getter
public enum ProjectRole {
  OWNER(EDIT, VIEW, DELETE, MANAGE_MEMBERS, VIEW_MEMBERS),
  EDITOR(Set.of(EDIT, VIEW, DELETE, VIEW_MEMBERS)),
  VIEWER(Set.of(VIEW, VIEW_MEMBERS));

  ProjectRole(Permission... permissions) {
    projectPermissions = Set.of(permissions);
  }

  private final Set<Permission> projectPermissions;
}

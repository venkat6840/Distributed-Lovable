package com.coding.distributed_lovable.workspace_service.service;

import com.coding.distributed_lovable.common_lib.enums.Permission;
import com.coding.distributed_lovable.workspace_service.dto.project.ProjectRequest;
import com.coding.distributed_lovable.workspace_service.dto.project.ProjectResponse;
import com.coding.distributed_lovable.workspace_service.dto.project.ProjectSummaryResponse;

import java.util.List;

public interface ProjectService {
  List<ProjectSummaryResponse> getUserProjects();

  ProjectSummaryResponse getProjectDetailsById(Long userId);

  ProjectResponse createProject(ProjectRequest projectRequest) ;

  ProjectResponse updateProject(Long id, ProjectRequest request);

  void softDelete(Long id);

  boolean hasPermission(Long projectId, Permission permission);
}

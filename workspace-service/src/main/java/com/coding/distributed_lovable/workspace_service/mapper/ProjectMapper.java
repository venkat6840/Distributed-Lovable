package com.coding.distributed_lovable.workspace_service.mapper;

import com.coding.distributed_lovable.common_lib.enums.ProjectRole;
import com.coding.distributed_lovable.workspace_service.dto.project.ProjectResponse;
import com.coding.distributed_lovable.workspace_service.dto.project.ProjectSummaryResponse;
import com.coding.distributed_lovable.workspace_service.entity.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

//  @Mapping(source = "owner", target = "profileResponse")
  ProjectResponse convertProjectToProjectResponse(Project project);

  ProjectSummaryResponse convertProjectToProjectSummary(Project project);

  ProjectSummaryResponse convertProjectToProjectSummaryResponse(Project project, ProjectRole role);
  
}

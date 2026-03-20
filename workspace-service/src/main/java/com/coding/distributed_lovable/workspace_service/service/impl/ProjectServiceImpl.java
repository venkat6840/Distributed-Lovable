package com.coding.distributed_lovable.workspace_service.service.impl;

import com.coding.distributed_lovable.common_lib.dto.PlanDto;
import com.coding.distributed_lovable.common_lib.enums.Permission;
import com.coding.distributed_lovable.common_lib.enums.ProjectRole;
import com.coding.distributed_lovable.common_lib.error.BadRequestException;
import com.coding.distributed_lovable.common_lib.error.ResourceNotFoundException;
import com.coding.distributed_lovable.common_lib.security.AuthUtil;
import com.coding.distributed_lovable.workspace_service.client.AccountClient;
import com.coding.distributed_lovable.workspace_service.dto.project.ProjectRequest;
import com.coding.distributed_lovable.workspace_service.dto.project.ProjectResponse;
import com.coding.distributed_lovable.workspace_service.dto.project.ProjectSummaryResponse;
import com.coding.distributed_lovable.workspace_service.entity.Project;
import com.coding.distributed_lovable.workspace_service.entity.ProjectMember;
import com.coding.distributed_lovable.workspace_service.entity.ProjectMemberId;
import com.coding.distributed_lovable.workspace_service.mapper.ProjectMapper;
import com.coding.distributed_lovable.workspace_service.repository.ProjectMemberRepository;
import com.coding.distributed_lovable.workspace_service.repository.ProjectRepository;
import com.coding.distributed_lovable.workspace_service.security.SecurityExpressions;
import com.coding.distributed_lovable.workspace_service.service.ProjectService;
import com.coding.distributed_lovable.workspace_service.service.ProjectTemplateService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

  private final ProjectRepository projectRepository;
  private final ProjectMapper projectMapper;
  private final ProjectMemberRepository projectMemberRepository;
  private final AuthUtil authUtil;
  private final ProjectTemplateService projectTemplateService;
  private final AccountClient accountClient;
  private final SecurityExpressions securityExpressions;

  @Override
  public List<ProjectSummaryResponse> getUserProjects() {
    Long userId = authUtil.getCurrentUserId();
    var projectsWithRoles = projectRepository.findAllAccessibleByUser(userId);
    //    if (userId != 0) {
    //      List<Project> projects = projectRepository.findByOwnerId(userId);
    //      if (!CollectionUtils.isEmpty(projects)) {
    //        return projects.stream().map(projectMapper::convertProjectToProjectSummary).toList();
    //      }
    //    }
    return projectsWithRoles.stream()
        .map(
            project ->
                projectMapper.convertProjectToProjectSummaryResponse(
                    project.getProject(), project.getRole()))
        .collect(Collectors.toUnmodifiableList());
  }

  @Override
  @PreAuthorize("@security.canViewProject(#projectId)")
  public ProjectSummaryResponse getProjectDetailsById(Long projectId) {
    Long userId = authUtil.getCurrentUserId();
    var projectWithRole =
        projectRepository
            .findAccessibleProjectByIdWithRole(projectId, userId)
            .orElseThrow(() -> new BadRequestException("Project not found"));
    return projectMapper.convertProjectToProjectSummaryResponse(
        projectWithRole.getProject(), projectWithRole.getRole());
  }

  @Override
  public ProjectResponse createProject(ProjectRequest projectRequest) {
    if (!canCreateNewProject()) {
      throw new BadRequestException(
          "User cannot create a new project with current plan upgrade plan now");
    }

    Long ownerUserId = authUtil.getCurrentUserId();

    Project project = Project.builder().name(projectRequest.name()).isPublic(false).build();
    project = projectRepository.save(project);
    ProjectMemberId projectMemberId = new ProjectMemberId(project.getId(), ownerUserId);

    ProjectMember projectMember =
        ProjectMember.builder()
            .id(projectMemberId)
            .projectRole(ProjectRole.OWNER)
            .project(project)
            .invitedAt(Instant.now())
            .acceptedAt(Instant.now())
            .build();
    projectMemberRepository.save(projectMember);

    projectTemplateService.initializeProjectFromTemplate(project.getId());

    return projectMapper.convertProjectToProjectResponse(project);
  }

  private boolean canCreateNewProject() {
    Long userId = authUtil.getCurrentUserId();
    if (userId == null) {
      return false;
    }

    PlanDto plan = accountClient.getCurrentSubscribedPlanByUser();
    int maxAllowed = plan.maxProjects();
    int ownedAccount = projectMemberRepository.countProjectsOwnedByUser(userId);
    return ownedAccount < maxAllowed;
  }

  @Override
  @PreAuthorize("@security.canEditProject(#projectId)")
  public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
    Long userId = authUtil.getCurrentUserId();
    Project project = getProjectById(projectId, userId);
    project.setName(request.name());
    project = projectRepository.save(project);

    return projectMapper.convertProjectToProjectResponse(project);
  }

  @Override
  @PreAuthorize("@security.canEditProject(#projectId)")
  public void softDelete(Long projectId) {
    Long userId = authUtil.getCurrentUserId();
    Project project = getProjectById(projectId, userId);
    project.setDeletedAt(Instant.now());
    projectRepository.save(project);
  }

  @Override
  public boolean hasPermission(Long projectId, Permission permission) {
    return securityExpressions.hasPermission(projectId, permission);
  }

  private Project getProjectById(Long id, Long userId) {
    return projectRepository
        .findAccessibleProjectById(id, userId)
        .orElseThrow(() -> new ResourceNotFoundException("Project", String.valueOf(id)));
  }
}

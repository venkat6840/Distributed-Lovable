package com.coding.distributed_lovable.workspace_service.service.impl;

import com.coding.distributed_lovable.common_lib.dto.UserDto;
import com.coding.distributed_lovable.common_lib.enums.ProjectRole;
import com.coding.distributed_lovable.common_lib.error.ResourceNotFoundException;
import com.coding.distributed_lovable.common_lib.security.AuthUtil;
import com.coding.distributed_lovable.workspace_service.client.AccountClient;
import com.coding.distributed_lovable.workspace_service.dto.member.InviteMemberRequest;
import com.coding.distributed_lovable.workspace_service.dto.member.MemberResponse;
import com.coding.distributed_lovable.workspace_service.dto.member.UpdateMemberRoleRequest;
import com.coding.distributed_lovable.workspace_service.entity.Project;
import com.coding.distributed_lovable.workspace_service.entity.ProjectMember;
import com.coding.distributed_lovable.workspace_service.entity.ProjectMemberId;
import com.coding.distributed_lovable.workspace_service.mapper.MemberResponseMapper;
import com.coding.distributed_lovable.workspace_service.repository.ProjectMemberRepository;
import com.coding.distributed_lovable.workspace_service.repository.ProjectRepository;
import com.coding.distributed_lovable.workspace_service.service.ProjectMemberService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

  private final ProjectRepository projectRepository;
  private final MemberResponseMapper memberResponseMapper;
  private final ProjectMemberRepository projectMemberRepository;
  private final AuthUtil authUtil;
  private final AccountClient accountClient;

  @Override
  @PreAuthorize("@security.canViewMembers(#projectId)")
  public List<MemberResponse> getProjectMembers(Long projectId) {
    return projectMemberRepository.findByIdProjectId(projectId).stream()
        .map(memberResponseMapper::convertProjectMemberToMemberResponse)
        .toList();
  }

  @Override
  @PreAuthorize("@security.canManageMembers(#projectId)")
  public MemberResponse inviteMember(Long projectId, InviteMemberRequest request) {
    Long userId = authUtil.getCurrentUserId();
    Project project = projectRepository.findById(projectId).orElseThrow();
    UserDto invitee =
        accountClient
            .getUserByEmail(request.username())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.username()));

    if (invitee.id().equals(userId)) {
      throw new RuntimeException("Cannot invite yourself");
    }

    ProjectMemberId projectMemberId = new ProjectMemberId(projectId, invitee.id());

    if (projectMemberRepository.existsById(projectMemberId)) {
      throw new RuntimeException("Cannot invite once again");
    }

    ProjectMember projectMember =
        ProjectMember.builder()
            .project(project)
            .projectRole(request.role())
            .id(projectMemberId)
            .build();
    projectMemberRepository.save(projectMember);

    return memberResponseMapper.convertProjectMemberToMemberResponse(projectMember);
  }

  @Override
  @PreAuthorize("@security.canManageMembers(#projectId)")
  public MemberResponse updateMemberRole(
      Long projectId, Long memberId, UpdateMemberRoleRequest request) {
    Project project = projectRepository.findById(projectId).orElseThrow();

    ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);

    ProjectMember projectMember = projectMemberRepository.findById(projectMemberId).orElseThrow();
    projectMember.setProjectRole(ProjectRole.VIEWER);
    projectMemberRepository.save(projectMember);

    return memberResponseMapper.convertProjectMemberToMemberResponse(projectMember);
  }

  @Override
  @PreAuthorize("@security.canManageMembers(#projectId)")
  public void softDelete(Long memberId, Long projectId) {
    Project project = projectRepository.findById(projectId).orElseThrow();

    ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
    if (!projectMemberRepository.existsById(projectMemberId)) {
      throw new RuntimeException("User is not a project member");
    }

    projectMemberRepository.deleteById(projectMemberId);
  }

  private Project getProjectById(Long id, Long userId) {
    return projectRepository.findAccessibleProjectById(id, userId).orElseThrow();
  }
}

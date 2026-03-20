package com.coding.distributed_lovable.workspace_service.service;


import com.coding.distributed_lovable.workspace_service.dto.member.InviteMemberRequest;
import com.coding.distributed_lovable.workspace_service.dto.member.MemberResponse;
import com.coding.distributed_lovable.workspace_service.dto.member.UpdateMemberRoleRequest;

import java.util.List;

public interface ProjectMemberService {
  List<MemberResponse> getProjectMembers(Long projectId);

  MemberResponse inviteMember(Long projectId, InviteMemberRequest request);

  MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request);

  void softDelete(Long memberId, Long projectId);
}

package com.coding.distributed_lovable.workspace_service.controller;

import com.coding.distributed_lovable.workspace_service.dto.member.InviteMemberRequest;
import com.coding.distributed_lovable.workspace_service.dto.member.MemberResponse;
import com.coding.distributed_lovable.workspace_service.dto.member.UpdateMemberRoleRequest;
import com.coding.distributed_lovable.workspace_service.service.ProjectMemberService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/members")
public class ProjectMemberController {
  private final ProjectMemberService projectMemberService;

  @GetMapping
  public ResponseEntity<List<MemberResponse>> getProjectMembers(@PathVariable Long projectId) {
    return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId));
  }

  @PostMapping
  public ResponseEntity<MemberResponse> inviteMember(
      @PathVariable Long projectId, @RequestBody @Valid InviteMemberRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(projectMemberService.inviteMember(projectId, request));
  }

  @PatchMapping("{memberId}")
  public ResponseEntity<MemberResponse> updateMemberRole(
      @PathVariable Long projectId,
      @PathVariable Long memberId,
      @RequestBody UpdateMemberRoleRequest request) {
    return ResponseEntity.ok(
        projectMemberService.updateMemberRole(projectId, memberId, request));
  }

  @DeleteMapping("/{memberId}")
  public ResponseEntity<Void> deleteMember(
      @PathVariable Long memberId, @PathVariable Long projectId) {
    projectMemberService.softDelete(memberId, projectId);
    return ResponseEntity.noContent().build();
  }
}

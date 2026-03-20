package com.coding.distributed_lovable.workspace_service.repository;

import java.util.List;
import java.util.Optional;

import com.coding.distributed_lovable.common_lib.enums.ProjectRole;
import com.coding.distributed_lovable.workspace_service.entity.ProjectMember;
import com.coding.distributed_lovable.workspace_service.entity.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

  List<ProjectMember> findByIdProjectId(Long projectId);

  @Query(
      "Select pm.projectRole from ProjectMember pm "
          + "where pm.id.projectId = :projectId and "
          + "pm.id.userId = :userId")
  Optional<ProjectRole> findRoleByProjectIdAndUserId(
      @Param("projectId") Long projectId, @Param("userId") Long userId);

  @Query(
      """
            select count(pm) from ProjectMember pm
            where pm.id.userId = :userId AND pm.projectRole = 'OWNER'
      """)
  int countProjectsOwnedByUser(@Param("userId") Long userId);
}

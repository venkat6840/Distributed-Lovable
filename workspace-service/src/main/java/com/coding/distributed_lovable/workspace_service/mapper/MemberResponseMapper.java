package com.coding.distributed_lovable.workspace_service.mapper;

import com.coding.distributed_lovable.workspace_service.dto.member.MemberResponse;
import com.coding.distributed_lovable.workspace_service.entity.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MemberResponseMapper {

  //@Mapping(target = "userId", source = "id")
  //@Mapping(target = "role", constant = "OWNER")
  //MemberResponse convertUserToMemberResponse(User user);

  @Mapping(target = "userId", source = "id.userId")
  MemberResponse convertProjectMemberToMemberResponse(ProjectMember projectMember);
}

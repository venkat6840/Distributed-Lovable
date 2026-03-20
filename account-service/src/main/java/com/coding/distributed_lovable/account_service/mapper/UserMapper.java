package com.coding.distributed_lovable.account_service.mapper;

import com.coding.distributed_lovable.account_service.dto.Auth.SignupRequest;
import com.coding.distributed_lovable.account_service.dto.Auth.UserProfileResponse;
import com.coding.distributed_lovable.account_service.entity.User;
import com.coding.distributed_lovable.common_lib.dto.UserDto;
import com.coding.distributed_lovable.common_lib.security.JwtUserPrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  User toEntity(SignupRequest request);

  @Mapping(source = "userId", target = "id")
  UserProfileResponse toUserProfileResponse(JwtUserPrincipal user);

  UserDto toUser(User user);
}

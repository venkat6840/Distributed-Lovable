package com.coding.distributed_lovable.common_lib.dto;

import org.jspecify.annotations.Nullable;

public record UserDto(Long id, String userName, String name, String password) {
    public void password(@Nullable String encode) {

    }
}

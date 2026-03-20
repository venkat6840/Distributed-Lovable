package com.coding.distributed_lovable.account_service.dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @Email @NotBlank String username,
        @NotBlank @Size(min = 1, max = 30) String name,
        @Size(min = 4) String password
) {
}

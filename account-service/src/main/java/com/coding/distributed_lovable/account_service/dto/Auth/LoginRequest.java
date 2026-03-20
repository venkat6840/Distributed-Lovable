package com.coding.distributed_lovable.account_service.dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @Author - Venkatesh G
 */
public record LoginRequest(
        @Email @NotBlank String username,
        @NotBlank @Size(min = 4, max = 50) String password
) {
}

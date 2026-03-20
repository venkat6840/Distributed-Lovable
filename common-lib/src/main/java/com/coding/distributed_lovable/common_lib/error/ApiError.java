package com.coding.distributed_lovable.common_lib.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;

public record ApiError(
    HttpStatus status,
    String message,
    Instant timestamp,
    @JsonInclude(JsonInclude.Include.NON_NULL) List<ApiFieldError> errors) {
  public ApiError(HttpStatus status, String message) {
    this(status, message, Instant.now(), null);
  }

  public ApiError(HttpStatus status, String message, List<ApiFieldError> apiFieldErrorList) {
    this(status, message, Instant.now(), apiFieldErrorList);
  }
}

record ApiFieldError(String field, String message) {};

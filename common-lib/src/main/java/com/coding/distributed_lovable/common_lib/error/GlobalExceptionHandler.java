package com.coding.distributed_lovable.common_lib.error;

import io.jsonwebtoken.JwtException;
import java.nio.file.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiError> handleBadRequestException(
      BadRequestException badRequestException) {
    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, badRequestException.getMessage());
    log.error(apiError.toString(), badRequestException);
    return ResponseEntity.status(apiError.status()).body(apiError);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleResourceNotFoundException(
      ResourceNotFoundException resourceNotFoundException) {
    ApiError apiError =
        new ApiError(
            HttpStatus.NOT_FOUND,
            resourceNotFoundException.getResourceName()
                + " with Id "
                + resourceNotFoundException.getResourceId()
                + " not found");
    log.error(apiError.toString(), resourceNotFoundException);
    return ResponseEntity.status(apiError.status()).body(apiError);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleInputValidationError(
      MethodArgumentNotValidException methodArgumentNotValidException) {
    var apiErrors =
        methodArgumentNotValidException.getBindingResult().getFieldErrors().stream()
            .map(error -> new ApiFieldError(error.getField(), error.getDefaultMessage()))
            .toList();

    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Input validation failed", apiErrors);
    log.error(apiError.toString(), methodArgumentNotValidException);
    return ResponseEntity.status(apiError.status()).body(apiError);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ApiError> handleUsernameNotFoundException(
      UsernameNotFoundException usernameNotFoundException) {
    ApiError apiError =
        new ApiError(
            HttpStatus.NOT_FOUND,
            "Username not found with username: " + usernameNotFoundException.getMessage());
    log.error(apiError.toString(), usernameNotFoundException);
    return ResponseEntity.status(apiError.status()).body(apiError);
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ApiError> handleUsernameJwtException(JwtException jwtException) {
    ApiError apiError =
        new ApiError(HttpStatus.UNAUTHORIZED, "Invalid Jwt Token: " + jwtException.getMessage());
    log.error(apiError.toString(), jwtException);
    return ResponseEntity.status(apiError.status()).body(apiError);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiError> handleUsernameJwtException(
      AuthenticationException authenticationException) {
    ApiError apiError =
        new ApiError(
            HttpStatus.UNAUTHORIZED,
            "Authentication failed: " + authenticationException.getMessage());
    log.error(apiError.toString(), authenticationException);
    return ResponseEntity.status(apiError.status()).body(apiError);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleUsernameJwtException(
      AccessDeniedException accessDeniedException) {
    ApiError apiError =
        new ApiError(
            HttpStatus.FORBIDDEN,
            "Access denied: Insufficient privileges for user: "
                + accessDeniedException.getMessage());
    log.error(apiError.toString(), accessDeniedException);
    return ResponseEntity.status(apiError.status()).body(apiError);
  }
}

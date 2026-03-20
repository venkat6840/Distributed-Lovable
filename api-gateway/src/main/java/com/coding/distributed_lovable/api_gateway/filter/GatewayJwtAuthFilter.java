package com.coding.distributed_lovable.api_gateway.filter;

import com.coding.distributed_lovable.api_gateway.config.SecurityProperties;
import com.coding.distributed_lovable.api_gateway.error.ApiError;
import com.coding.distributed_lovable.api_gateway.service.JwtGatewayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class GatewayJwtAuthFilter implements GlobalFilter, Ordered {

  private final SecurityProperties securityProperties;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();
  private final JwtGatewayService jwtGatewayService;
  private final ObjectMapper objectMapper =
      (ObjectMapper) new ObjectMapper().registerModule(new JavaTimeModule());

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

    ServerHttpRequest request = exchange.getRequest();
    String path = request.getPath().toString();

    boolean isPublic =
        securityProperties.getPublicRoutes().stream()
            .anyMatch(pattern -> pathMatcher.match(pattern, path));

    if (isPublic) {
      log.info("Public route, continue: {}", path);
      return chain.filter(exchange);
    }

    String authHeader = request.getHeaders().getFirst("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.error("Missing or invalid authorization header for path : {}", path);
      return sendErrorResponse(
          exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid authorization header");
    }

    // If the token is present, extract the token from the header
    String tokenHeader = authHeader.split("Bearer ")[1];

    try {
      jwtGatewayService.validateToken(tokenHeader);
      log.info("Jwt token valid for path : {}", path);
    } catch (Exception e) {
      log.error("Jwt token validation failed at gateway: {}", e.getMessage());
      return sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    return chain.filter(exchange);
  }

  private Mono<Void> sendErrorResponse(
      ServerWebExchange exchange, HttpStatus httpStatus, String message) {
    exchange.getResponse().setStatusCode(httpStatus);
    exchange.getResponse().getHeaders().add("Content-Type", "application/json");
    ApiError error = new ApiError(httpStatus, message);
    try {
      byte[] bytes = objectMapper.writeValueAsBytes(error);
      DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
      return exchange.getResponse().writeWith(Mono.just(buffer));

    } catch (Exception e) {
      log.error("Error serializing gateway error response", e);
      return exchange.getResponse().setComplete();
    }
  }

  @Override
  public int getOrder() {
    return 0;
  }
}

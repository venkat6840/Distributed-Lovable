package com.coding.distributed_lovable.common_lib.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final AuthUtil authUtil;
  private final HandlerExceptionResolver handlerExceptionResolver;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      // Fetch header details from the authentication context
      final String header = request.getHeader("Authorization");

      // If there is no token return and continue with other filter chains
      if (header == null || !header.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
      }

      // If the token is present, extract the token from the header
      String tokenHeader = header.split("Bearer ")[1];
      JwtUserPrincipal jwtUserPrincipal = authUtil.parseAccessToken(tokenHeader);

      // Set/Add the token into Security context
      if (jwtUserPrincipal != null
          || SecurityContextHolder.getContext().getAuthentication() == null) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
            new UsernamePasswordAuthenticationToken(
                jwtUserPrincipal, tokenHeader, jwtUserPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
      }
      filterChain.doFilter(request, response);
    } catch (Exception e) {
      handlerExceptionResolver.resolveException(request, response, null, e);
    }
  }
}

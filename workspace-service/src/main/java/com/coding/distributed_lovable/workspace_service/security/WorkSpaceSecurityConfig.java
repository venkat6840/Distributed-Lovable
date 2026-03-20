package com.coding.distributed_lovable.workspace_service.security;

import com.coding.distributed_lovable.common_lib.security.JwtAuthFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class WorkSpaceSecurityConfig {
  private final JwtAuthFilter jwtAuthFilter;
  private final HandlerExceptionResolver handlerExceptionResolver;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
    return httpSecurity
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(
            sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.dispatcherTypeMatchers(DispatcherType.ASYNC)
                    .permitAll()
                    .dispatcherTypeMatchers(DispatcherType.ERROR)
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        // .formLogin(Customizer.withDefaults())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            exceptionHandlerConfigurer ->
                exceptionHandlerConfigurer.accessDeniedHandler(
                    (request, response, accessDeniedException) -> {
                      handlerExceptionResolver.resolveException(
                          request, response, null, accessDeniedException);
                    }))
        .build();
  }
}

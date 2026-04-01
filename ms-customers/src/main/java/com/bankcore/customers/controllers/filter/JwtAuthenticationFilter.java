package com.bankcore.customers.controllers.filter;

import com.bankcore.customers.exceptions.NoAuthoritiesException;
import com.bankcore.customers.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Filter responsible for validating JSON Web Tokens (JWT) on every incoming HTTP request.
 *
 * <p>This filter intercepts requests, extracts the JWT from the "Authorization" header, validates
 * it, and sets the security context if the token is valid. It extends {@link OncePerRequestFilter}
 * to ensure a single execution per request dispatch.
 *
 * @author BankCore Team - Cristian Ortiz
 * @version 0.1.0
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final HandlerExceptionResolver exceptionResolver;
  private final JwtService jwtService;

  public JwtAuthenticationFilter(
      JwtService jwtService,
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
    this.jwtService = jwtService;
    this.exceptionResolver = exceptionResolver;
  }

  /**
   * Intercepts the request to perform JWT authentication.
   *
   * <p>If a valid JWT is found: 1. Extracts the user UUID and roles. 2. Creates an {@link
   * UsernamePasswordAuthenticationToken}. 3. Sets remote address and session details. 4. Updates
   * the {@link SecurityContextHolder}.
   *
   * @param request The incoming {@link HttpServletRequest}.
   * @param response The outgoing {@link HttpServletResponse}.
   * @param filterChain The {@link FilterChain} to execute the next filter.
   * @throws ServletException If a servlet-specific error occurs.
   * @throws IOException If an I/O error occurs during processing.
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String jwt = parseJwt(request);

      if (jwt != null && jwtService.validateToken(jwt)) {

        String subject = jwtService.getUuidFromToken(jwt);

        List<String> roles = jwtService.getRolesFromToken(jwt);

        if (roles != null && !roles.isEmpty()) {
          List<GrantedAuthority> authorities =
              roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(subject, null, authorities);

          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
          throw new NoAuthoritiesException("User has no granted authorities");
        }
      }
    } catch (Exception e) {
      log.error("Could not authenticate user: {}", e.getMessage(), e);
      exceptionResolver.resolveException(request, response, null, e);
      return;
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Extracts the JWT token from the Authorization header.
   *
   * <p>The method checks if the header starts with "Bearer " and returns the raw token.
   *
   * @param request The current HTTP request.
   * @return The JWT token string, or {@code null} if not found or malformed.
   */
  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");
    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }
    return null;
  }
}

package com.bankcore.accounts.exceptions;

import com.bankcore.accounts.dto.responses.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Custom implementation of {@link AuthenticationEntryPoint} to handle 401 Unauthorized errors.
 *
 * <p>This component is triggered when an unauthenticated user attempts to access a protected
 * resource. It overrides the default Spring Security behavior to return a structured JSON response
 * using {@link ErrorResponse}.
 *
 * @author BankCore Team - Crisian Ortiz - Sebastian Orjuela
 * @version 0.1.0
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  /** Object mapper used for serializing the unauthorized error body into JSON format. */
  private final ObjectMapper objectMapper;

  /**
   * Commences the authentication scheme.
   *
   * <p>This method is invoked when a user tries to access a secured REST endpoint without providing
   * valid credentials.
   *
   * @param request The {@link HttpServletRequest} that resulted in an AuthenticationException.
   * @param response The {@link HttpServletResponse} so that the user agent can begin authentication
   *     or receive the error.
   * @param authException The exception that caused the invocation (e.g., BadCredentialsException).
   * @throws IOException If an input or output exception occurs during response writing.
   * @throws ServletException If a servlet-related error occurs.
   */
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    ErrorResponse body =
        ErrorResponse.builder()
            .code(HttpStatus.UNAUTHORIZED.value())
            .name(HttpStatus.UNAUTHORIZED.getReasonPhrase())
            .description("Authentication is required to access this resource.")
            .build();

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), body);
  }
}

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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Custom implementation of {@link AccessDeniedHandler} to handle 403 Forbidden errors.
 *
 * <p>This component intercepts authorization failures when an authenticated user attempts to access
 * a protected resource without the necessary permissions. It returns a standardized JSON response
 * using {@link ErrorResponse}.
 *
 * @author BankCore Team - Crisian Ortiz - Sebastian Orjuela
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  /** Object mapper for serializing the error response body to JSON. */
  private final ObjectMapper objectMapper;

  /**
   * Handles access denied failure by writing a custom JSON error message to the response.
   *
   * @param request The {@link HttpServletRequest} that resulted in an AccessDeniedException.
   * @param response The {@link HttpServletResponse} so that the user agent can be advised of the
   *     failure.
   * @param accessDeniedException The exception that caused the invocation.
   * @throws IOException If an input or output exception occurs when the handler writes the
   *     response.
   * @throws ServletException If a servlet-related error occurs.
   */
  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException, ServletException {

    ErrorResponse body =
        ErrorResponse.builder()
            .code(HttpStatus.FORBIDDEN.value())
            .name(HttpStatus.FORBIDDEN.getReasonPhrase())
            .description("You do not have permission to access this resource.")
            .build();

    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), body);
  }
}

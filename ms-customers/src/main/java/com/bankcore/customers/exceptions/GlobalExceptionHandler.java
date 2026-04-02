package com.bankcore.customers.exceptions;

import com.bankcore.customers.dto.responses.ErrorResponse;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler responsible for centralizing application-wide error handling logic.
 *
 * <p>This component intercepts exceptions thrown by controllers and transforms them into structured
 * {@link ErrorResponse} objects, ensuring consistent API error responses across the system.
 *
 * @author Bankcore Team - Sebastian Orjuela - Cristian Ortiz
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles {@link ResourceConflictException} thrown when a resource already exists or violates a
   * uniqueness constraint.
   *
   * @param ex the thrown {@code ResourceConflictException}
   * @return a {@link ResponseEntity} containing a structured error response with HTTP status {@code
   *     409 Conflict}
   */
  @ExceptionHandler(ResourceConflictException.class)
  public ResponseEntity<ErrorResponse> handleResourceConflictException(
      ResourceConflictException ex) {

    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  /**
   * Handles validation errors triggered by {@link jakarta.validation.Valid} when request payloads
   * fail Bean Validation constraints.
   *
   * <p>Aggregates all field validation errors into a single descriptive message.
   *
   * @param ex the {@link MethodArgumentNotValidException} containing validation details
   * @return a {@link ResponseEntity} containing a structured error response with HTTP status {@code
   *     400 Bad Request}
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {

    String description =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));

    return buildErrorResponse(HttpStatus.BAD_REQUEST, description);
  }

  /**
   * Handles exceptions when an authenticated user's profile is not found in the database.
   *
   * <p>This handler is triggered by {@link UserProfileNotFoundException}. It logs a security alert
   * since this scenario typically implies an inconsistency between the security context (JWT) and
   * the persistence layer.
   *
   * @param ex The {@link UserProfileNotFoundException} instance caught by the advice.
   * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with HTTP status 404 (Not
   *     Found) and a user-friendly message.
   */
  @ExceptionHandler(UserProfileNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFound(UserProfileNotFoundException ex) {

    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  /**
   * Handles {@link NoAuthoritiesException} globally across the application.
   *
   * <p>This method logs the security breach or missing permission error and returns a standardized
   * error response with a 403 Forbidden status.
   *
   * @param ex the caught NoAuthoritiesException containing the error details.
   * @return a {@link ResponseEntity} containing an {@code ErrorResponse} object with the HTTP
   *     status and a user-friendly error message.
   */
  @ExceptionHandler(NoAuthoritiesException.class)
  public ResponseEntity<ErrorResponse> handleNoAuthoritiesException(NoAuthoritiesException ex) {
    log.error("NoAuthoritiesException: {}", ex.getMessage());

    return buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied");
  }

  /**
   * Handles Spring Security {@link org.springframework.security.core.AuthenticationException} and
   * all its subclasses, including but not limited to:
   *
   * <ul>
   *   <li>{@link org.springframework.security.authentication.BadCredentialsException} – wrong
   *       password
   *   <li>{@link org.springframework.security.core.userdetails.UsernameNotFoundException} – user
   *       not found
   *   <li>{@link org.springframework.security.authentication.LockedException} – account locked
   *   <li>{@link org.springframework.security.authentication.DisabledException} – account disabled
   * </ul>
   *
   * <p>Returns a {@code 401 Unauthorized} response with a generic message to prevent user
   * enumeration attacks (i.e., avoid revealing whether the email exists in the system).
   *
   * @param ex the {@code AuthenticationException} thrown during the authentication process
   * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with HTTP 401 status
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
    log.error("Authentication failed: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication failed.");
  }

  /**
   * Handles {@link MethodArgumentTypeMismatchException} thrown when a request parameter cannot be
   * converted to the expected type.
   *
   * <p>This exception handler is triggered when a client provides an invalid format for a request
   * parameter, such as an incorrectly formatted UUID. It inspects the expected type of the
   * parameter and generates a descriptive error message to help the client correct their request.
   *
   * @param ex the {@link MethodArgumentTypeMismatchException} instance containing details about the
   *     invalid argument, including its name and expected type.
   * @return ResponseEntity containing an {@link ErrorResponse} object with a descriptive error
   *     message and HTTP status 400 (Bad Request).
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex) {

    String expectedType =
        ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";

    String description =
        String.format(
            "Invalid format for parameter '%s'. Expected type: %s", ex.getName(), expectedType);

    return buildErrorResponse(HttpStatus.BAD_REQUEST, description);
  }

  /**
   * Builds a standardized {@link ErrorResponse} object.
   *
   * @param status the HTTP status associated with the error
   * @param description a human-readable description of the error
   * @return a {@link ResponseEntity} wrapping the constructed error body
   */
  private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String description) {

    ErrorResponse body =
        ErrorResponse.builder()
            .code(status.value())
            .name(status.getReasonPhrase())
            .description(description)
            .build();

    return ResponseEntity.status(status).body(body);
  }
}

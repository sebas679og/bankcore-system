package com.bankcore.accounts.exceptions;

import com.bankcore.accounts.dto.responses.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for handling custom exceptions in the accounts service.
 *
 * <p>This class intercepts specific exceptions thrown by the service and returns a structured JSON
 * response with details about the error using the {@link ErrorResponse} DTO.
 *
 * @author BankCore Team - Sebastian Orjuela - Cristian Ortiz
 * @version 0.1.0
 */
@SuppressWarnings("PMD.TooManyMethods")
@RestControllerAdvice
public class GlobalExceptionHandler {

  // Handle custom exceptions related to customer status and resource conflicts
  @ExceptionHandler(CustomerInactiveException.class)
  public ResponseEntity<ErrorResponse> handleCustomerInactiveException(
      CustomerInactiveException ex) {
    return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  // Handle custom exception for when a customer is not found
  @ExceptionHandler(CustomerNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(
      CustomerNotFoundException ex) {
    return notFound(ex.getMessage());
  }

  // Handle custom exception for resource conflicts, such as duplicate entries
  @ExceptionHandler(ResourceConflictException.class)
  public ResponseEntity<ErrorResponse> handleResourceConflictException(
      ResourceConflictException ex) {
    return conflict(ex.getMessage());
  }

  // Handle custom business logic exceptions that may occur during processing
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
    return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
  }

  // Handle custom exceptions related to failures in external service calls
  @ExceptionHandler(CustomExternalServiceException.class)
  public ResponseEntity<ErrorResponse> handleExternalServiceError(
      CustomExternalServiceException ex) {
    return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
  }

  /**
   * Handle exceptions related to malformed JSON in the request body, such as invalid enum values or
   * incorrect data types. This method provides detailed error messages for easier debugging.
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> customHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {
    Throwable cause = ex.getCause();
    String message = "Invalid request payload";

    if (cause instanceof InvalidFormatException formatException
        && formatException.getTargetType().isEnum()) {

      String fieldName = formatException.getPath().get(0).getFieldName();
      Object invalidValue = formatException.getValue();

      String allowedValues =
          Arrays.stream(formatException.getTargetType().getEnumConstants())
              .map(Object::toString)
              .collect(Collectors.joining(", "));

      message =
          String.format(
              "%s: Invalid value '%s'. Allowed values: [%s]",
              fieldName, invalidValue, allowedValues);
    }

    return badRequest(message);
  }

  /**
   * Handle validation errors for request bodies annotated with @Valid. This method extracts
   * field-level error messages and compiles them into a single response for easier debugging.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> customMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    String description =
        ex.getBindingResult().getAllErrors().stream()
            .map(
                error -> {
                  if (error instanceof FieldError fieldError) {
                    return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                  }
                  return error.getDefaultMessage();
                })
            .filter(msg -> msg != null && !msg.isBlank())
            .collect(Collectors.joining("; "));

    return badRequest(description);
  }

  // Handles custom exceptions - system validation error capture
  @ExceptionHandler(CustomInternalServiceException.class)
  public ResponseEntity<ErrorResponse> handleCustomInvalidParameter(
      CustomInternalServiceException ex) {
    return buildErrorResponse(HttpStatus.BAD_GATEWAY, ex.getMessage());
  }

  /**
   * Handles type mismatch errors for request parameters, such as when a string is provided where an
   * integer is expected. This method provides detailed error messages indicating the invalid value
   * and the expected parameter type.
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    String value = ex.getValue() != null ? ex.getValue().toString() : "null";
    String message = String.format("Invalid value '%s' for parameter '%s'", value, ex.getName());
    return badRequest(message);
  }

  // Handles inactive account errors - returns HTTP 409 Conflict
  @ExceptionHandler(AccountInactiveException.class)
  public ResponseEntity<ErrorResponse> handleAccountInactiveException(AccountInactiveException ex) {
    return conflict(ex.getMessage());
  }

  // Handles account not found errors - returns HTTP 404 Not Found
  @ExceptionHandler(AccountNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException ex) {
    return notFound(ex.getMessage());
  }

  // Handles temporarily locked account errors - returns HTTP 423 Locked
  @ExceptionHandler(AccountTemporarilyLockedException.class)
  public ResponseEntity<ErrorResponse> handleAccountTemporarilyLockedException(
      AccountTemporarilyLockedException ex) {
    return locked(ex.getMessage());
  }

  // Handles permanently locked account errors - returns HTTP 423 Locked
  @ExceptionHandler(AccountPermanentlyLockedException.class)
  public ResponseEntity<ErrorResponse> handleAccountPermanentlyLockedException(
      AccountPermanentlyLockedException ex) {
    return locked(ex.getMessage());
  }

  // Handles incorrect PIN attempts - returns HTTP 400 Bad Request
  @ExceptionHandler(IncorrectPinException.class)
  public ResponseEntity<ErrorResponse> handleIncorrectPinException(IncorrectPinException ex) {
    return badRequest(ex.getMessage());
  }

  // Handles insufficient funds - returns HTTP 409 Conflict
  @ExceptionHandler(InsufficientBalanceException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(
      InsufficientBalanceException ex) {
    return conflict(ex.getMessage());
  }

  // Handles exception of transfer to destination account - returns HTTP 409 Conflict
  @ExceptionHandler(InvalidTransferDestinationException.class)
  public ResponseEntity<ErrorResponse> handleInvalidTransferDestinationException(
      InvalidTransferDestinationException ex) {
    return conflict(ex.getMessage());
  }

  /**
   * Handles validation errors for request parameters annotated with @Validated. This method
   * extracts constraint violation messages and compiles them into a single response for easier
   * debugging.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      ConstraintViolationException ex) {
    String message =
        ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining("; "));

    return badRequest(message);
  }

  /**
   * Helper method for building a 400 BAD_REQUEST error response.
   *
   * @param message the error message
   * @return a BAD_REQUEST {@link ErrorResponse}
   */
  private ResponseEntity<ErrorResponse> badRequest(String message) {
    return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
  }

  /**
   * Helper method for building a 404 NOT_FOUND error response.
   *
   * @param message the error message
   * @return a NOT_FOUND {@link ErrorResponse}
   */
  private ResponseEntity<ErrorResponse> notFound(String message) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, message);
  }

  /**
   * Helper method for building a 409 CONFLICT error response.
   *
   * @param message the error message
   * @return a CONFLICT {@link ErrorResponse}
   */
  private ResponseEntity<ErrorResponse> conflict(String message) {
    return buildErrorResponse(HttpStatus.CONFLICT, message);
  }

  /**
   * Helper method for building a 423 LOCKED error response.
   *
   * @param message the error message
   * @return a LOCKED {@link ErrorResponse}
   */
  private ResponseEntity<ErrorResponse> locked(String message) {
    return buildErrorResponse(HttpStatus.LOCKED, message);
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

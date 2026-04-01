package com.bankcore.customers.exceptions;

/**
 * Exception thrown when an attempt is made to create a resource that already exists, typically
 * during registration.
 *
 * @author Bankcore Team - Sebastian Orjuea
 * @version 0.1.0
 */
public class ResourceConflictException extends RuntimeException {

  /**
   * Constructs a new {@code ResourceConflictException} with the specified detail message.
   *
   * @param message the detail message explaining the cause of the exception
   */
  public ResourceConflictException(String message) {
    super(message);
  }
}

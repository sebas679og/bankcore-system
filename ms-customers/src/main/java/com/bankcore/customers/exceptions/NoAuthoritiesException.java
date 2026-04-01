package com.bankcore.customers.exceptions;

/**
 * Exception thrown when a user attempts to perform an action but possesses no granted authorities
 * or permissions within the security context.
 *
 * <p>This is a {@link RuntimeException}, meaning it does not need to be explicitly declared in a
 * method's {@code throws} clause.
 *
 * @author BankCore Team - Cristian Ortiz
 * @since 0.1.0
 */
public class NoAuthoritiesException extends RuntimeException {

  /**
   * Constructs a new NoAuthoritiesException with a specific detail message.
   *
   * @param message the detail message explaining why the exception was thrown (usually indicating
   *     which user lacks permissions).
   */
  public NoAuthoritiesException(String message) {
    super(message);
  }
}

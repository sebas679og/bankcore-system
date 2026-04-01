package com.bankcore.customers.exceptions;

/**
 * Exception thrown when a customer profile cannot be found in the system.
 *
 * <p>This typically occurs when a search is performed by a unique identifier (like username, email
 * or ID) that does not exist in the database. This exception is usually handled to return a 404 Not
 * Found HTTP status.
 *
 * @author BankCore Team
 * @author Cristian Ortiz
 * @version 0.1.0
 */
public class UserProfileNotFoundException extends RuntimeException {

  /**
   * Constructs a new UserProfileNotFoundException with a specific detail message.
   *
   * @param message The detail message explaining the reason for the failure.
   */
  public UserProfileNotFoundException(String message) {
    super(message);
  }
}

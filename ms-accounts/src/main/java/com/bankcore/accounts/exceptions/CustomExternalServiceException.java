package com.bankcore.accounts.exceptions;

/**
 * Custom exception class for handling errors related to external service calls in the accounts
 * service.
 *
 * @author BankCore Team - Sebastian Orjuela
 * @version 1.0
 */
public class CustomExternalServiceException extends RuntimeException {
  public CustomExternalServiceException(String message) {
    super(message);
  }
}

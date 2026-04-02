package com.bankcore.accounts.exceptions;

/**
 * Custom exception class to handle errors related to ms-customers processing errors.
 *
 * @author BankCore Team - Sebastian Orjuela
 * @version 1.0
 */
public class CustomInternalServiceException extends RuntimeException {

  public CustomInternalServiceException(String message) {
    super(message);
  }
}

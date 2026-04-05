package com.bankcore.accounts.exceptions;

/**
 * Custom exception class for handling resource conflicts in the accounts service.
 *
 * @author BankCore Team - Sebastian Orjuela
 * @version 0.1.0
 */
public class ResourceConflictException extends RuntimeException {

  public ResourceConflictException(String message) {
    super(message);
  }
}

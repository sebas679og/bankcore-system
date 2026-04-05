package com.bankcore.accounts.exceptions;

/**
 * Custom exception class for business logic errors in the accounts service.
 *
 * @author BankCore Team - Sebastian Orjuela
 * @version 0.1.0
 */
public class BusinessException extends RuntimeException {

  public BusinessException(String message) {
    super(message);
  }
}

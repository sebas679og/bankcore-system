package com.bankcore.accounts.exceptions;

/**
 * Custom exception class for handling cases where a customer is inactive in the accounts service.
 *
 * @author BankCore Team - Sebastian Orjuela
 * @version 1.0
 */
public class CustomerInactiveException extends RuntimeException {

  public CustomerInactiveException(String message) {
    super(message);
  }
}

package com.bankcore.accounts.exceptions;

/**
 * Custom exception class for handling cases where a customer is not found in the accounts service.
 *
 * @author BankCore Team - Sebastian Orjuela
 * @version 0.1.0
 */
public class CustomerNotFoundException extends RuntimeException {
  public CustomerNotFoundException(String message) {
    super(message);
  }
}

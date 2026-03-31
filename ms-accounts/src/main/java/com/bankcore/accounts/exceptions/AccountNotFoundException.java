package com.bankcore.accounts.exceptions;

/**
 * Exception thrown to indicate that an account could not be found.
 *
 * <p>This runtime exception is typically used in account management or authentication workflows
 * when a requested account identifier does not correspond to any existing account in the system.
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 1.1
 * @see RuntimeException
 */
public class AccountNotFoundException extends RuntimeException {
  public AccountNotFoundException() {
    super("Account not found");
  }

  public AccountNotFoundException(String message) {
    super(message);
  }
}

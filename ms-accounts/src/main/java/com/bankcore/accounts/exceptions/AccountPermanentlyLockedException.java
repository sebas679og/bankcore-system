package com.bankcore.accounts.exceptions;

/**
 * Exception thrown to indicate that an account has been permanently locked.
 *
 * <p>This runtime exception is typically used in account management or authentication workflows to
 * signal that an account is blocked and cannot be reactivated. It represents a terminal state where
 * no further login or recovery attempts are allowed.
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 0.1.0
 * @see RuntimeException
 */
public class AccountPermanentlyLockedException extends RuntimeException {
  public AccountPermanentlyLockedException() {
    super("Account permanently blocked");
  }
}

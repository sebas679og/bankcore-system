package com.bankcore.accounts.exceptions;

import com.bankcore.accounts.utils.enums.AccountStatus;

/**
 * Exception thrown to indicate that an account is inactive.
 *
 * <p>This runtime exception is typically used in authentication or account management workflows to
 * signal that an operation cannot proceed because the account is not in an active state.
 *
 * <p>The exception message includes the specific {@link AccountStatus} value that caused the
 * failure, providing additional context for debugging or client-facing error handling.
 *
 * @see RuntimeException
 * @see AccountStatus
 * @author Bankcore Team - Sebastian Orjuela
 * @version 1.1
 */
public class AccountInactiveException extends RuntimeException {

  public AccountInactiveException(AccountStatus status) {
    super(String.join(" ", "Account is not active. account status is", status.name()));
  }

  public AccountInactiveException(AccountStatus status, String message) {
    super(String.join(" ", message, "account status is", status.name()));
  }
}

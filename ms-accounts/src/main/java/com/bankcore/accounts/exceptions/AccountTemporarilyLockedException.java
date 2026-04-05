package com.bankcore.accounts.exceptions;

import java.time.Instant;

/**
 * Exception thrown to indicate that an account has been temporarily locked.
 *
 * <p>This runtime exception is typically used in authentication or account management workflows to
 * signal that an account is blocked for a limited period of time. The lock duration is represented
 * by an {@link Instant} value, after which the account may become available again.
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 0.1.0
 * @see RuntimeException
 * @see java.time.Instant
 */
public class AccountTemporarilyLockedException extends RuntimeException {
  public AccountTemporarilyLockedException(Instant until) {
    super("Account temporarily locked until " + until);
  }
}

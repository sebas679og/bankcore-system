package com.bankcore.accounts.exceptions;

/**
 * Exception thrown to indicate that an incorrect PIN was entered.
 *
 * <p>This runtime exception is typically used in authentication workflows where a user must provide
 * a valid PIN. Each failed attempt reduces the number of remaining attempts before the account is
 * temporarily locked.
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 0.1.0
 * @see RuntimeException
 */
public class IncorrectPinException extends RuntimeException {
  public IncorrectPinException(int remainingAttempts) {
    super("Incorrect PIN. You have " + remainingAttempts + " attempts left.");
  }
}

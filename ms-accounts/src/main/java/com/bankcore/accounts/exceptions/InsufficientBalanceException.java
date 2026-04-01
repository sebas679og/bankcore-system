package com.bankcore.accounts.exceptions;

/**
 * Exception thrown when an account does not have sufficient balance to complete a transfer or
 * withdrawal operation.
 *
 * <p>This is a runtime exception, typically used in service or business logic layers to signal that
 * the requested operation cannot be performed due to insufficient funds.
 *
 * @author BankcoreTeam - Sebastian Orjuela
 * @version 0.1.0
 */
public class InsufficientBalanceException extends RuntimeException {
  public InsufficientBalanceException() {
    super("Insufficient balance");
  }
}

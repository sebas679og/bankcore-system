package com.bankcore.accounts.exceptions;

/**
 * Exception thrown when a transfer destination is invalid.
 *
 * <p>This runtime exception is typically used in business or service layers to indicate that the
 * provided destination account or IBAN does not meet the required validation rules for a transfer
 * operation.
 *
 * @author BankcoreTeam - Sebastian Orjuela
 * @version 1.0
 */
public class InvalidTransferDestinationException extends RuntimeException {
  public InvalidTransferDestinationException(String message) {
    super(message);
  }
}

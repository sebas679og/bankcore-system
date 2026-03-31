package com.bankcore.accounts.utils.enums;

/**
 * Represents the type of a financial transaction within the system.
 *
 * <p>Each value describes the direction and nature of the monetary movement associated with a
 * {@code TransactionEntity}.
 *
 * @author BankCore Team - Cristian Ortiz
 */
public enum TransactionType {

  /** Funds added to the account from an external source. */
  DEPOSIT,

  /** Funds removed from the account by the account holder. */
  WITHDRAWAL,

  /** Funds received into the account as part of a transfer from another account. */
  TRANSFER_IN,

  /** Funds sent out of the account as part of a transfer to another account. */
  TRANSFER_OUT,

  /** A charge applied to the account for a service or penalty. */
  FEE
}

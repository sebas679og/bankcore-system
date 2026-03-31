package com.bankcore.accounts.utils.enums;

/**
 * Represents the processing status of a financial transaction.
 *
 * <p>Tracks the lifecycle state of a {@code TransactionEntity} from creation through to final
 * settlement or failure.
 *
 * @author BankCore Team - Cristian Ortiz
 */
public enum TransactionStatus {

  /** The transaction was successfully processed and settled. */
  COMPLETED,

  /** The transaction has been initiated but not yet processed. */
  PENDING,

  /** The transaction could not be completed due to an error or rejection. */
  FAILED,

  /** A previously completed transaction that has been reversed or refunded. */
  REVERSED
}

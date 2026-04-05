package com.bankcore.accounts.utils.enums;

/**
 * Represents the possible states of a financial or data transfer process.
 *
 * <p>This enumeration is typically used to track the lifecycle of a transfer operation, from
 * initiation to completion or cancellation.
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 0.1.0
 */
public enum TransferStatus {
  /** The transfer has been created but not yet processed. */
  PENDING,

  /** The transfer finished successfully. */
  COMPLETED,

  /** The transfer was attempted but did not succeed. */
  FAILED,

  /** The transfer is planned to occur at a future time. */
  SCHEDULED,

  /** The transfer was stopped before completion. */
  CANCELLED
}

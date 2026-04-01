package com.bankcore.customers.utils.enums;

/**
 * Represents the possible states of a Customer account within the system lifecycle.
 *
 * <p>This status controls authentication behavior, transactional permissions, and account access
 * rules.
 *
 * @author Bankcore Team
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
public enum CustomerStatus {

  /**
   * Customer account is fully active and operational. The user can authenticate and perform
   * transactions.
   */
  ACTIVE,

  /** Customer account is temporarily inactive. Authentication or transactions may be restricted. */
  INACTIVE,

  /** Customer account has been blocked due to security reasons or policy violations. */
  BLOCKED,

  /** Customer account has been created but requires verification (e.g., email or KYC). */
  PENDING_VERIFICATION
}

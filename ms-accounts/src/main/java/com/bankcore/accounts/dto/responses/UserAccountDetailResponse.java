package com.bankcore.accounts.dto.responses;

import com.bankcore.accounts.utils.enums.AccountStatus;
import com.bankcore.accounts.utils.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

/**
 * Response DTO containing detailed information about a user's bank account. Used in endpoints that
 * require full account details beyond the summary view.
 *
 * @author BankCore Team - Cristian Ortiz
 * @version 0.1.0
 */
@Value
@Builder
public class UserAccountDetailResponse {

  /** Unique identifier of the account. */
  UUID id;

  /** The account number used for transactions and identification. */
  String accountNumber;

  /** Unique identifier of the customer who owns this account. */
  UUID customerId;

  /** The type of account. */
  AccountType accountType;

  /** Currency code associated with the account (e.g., USD, EUR). */
  String currency;

  /** Current available balance of the account. */
  BigDecimal balance;

  /** Optional user-defined alias or label for the account. */
  String alias;

  /** Current status of the account (e.g., ACTIVE, INACTIVE, etc.). */
  AccountStatus status;

  /** Timestamp indicating when the account was created. */
  Instant createdAt;

  /** Timestamp of the most recent transaction performed on this account. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Instant lastTransactionAt;
}

package com.bankcore.accounts.dto.responses;

import com.bankcore.accounts.utils.enums.AccountStatus;
import com.bankcore.accounts.utils.enums.AccountType;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

/**
 * Response DTO representing a bank account belonging to the authenticated customer.
 *
 * <p>Returned as part of the account listing endpoint, containing the core details of each account
 * associated with the user.
 *
 * @author BankCore Team - Cristian Ortiz
 */
@Value
@Builder
public class UserAccountResponse {

  /** Unique identifier of the account. */
  UUID id;

  /** The account number used for transactions and identification. */
  String accountNumber;

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
}

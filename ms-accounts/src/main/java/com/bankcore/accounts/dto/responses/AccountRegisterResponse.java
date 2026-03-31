package com.bankcore.accounts.dto.responses;

import com.bankcore.accounts.utils.enums.AccountStatus;
import com.bankcore.accounts.utils.enums.AccountType;
import com.bankcore.accounts.utils.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Response DTO for account registration.
 *
 * @author BankCore Team - Sebastian Orjuela
 * @version 1.0
 */
@Setter
@Builder
@Getter
public class AccountRegisterResponse {

  private UUID id;
  private String accountNumber;
  private UUID customerId;
  private AccountType accountType;
  private CurrencyCode currency;
  private BigDecimal balance;
  private String alias;
  private AccountStatus status;
  private Instant createdAt;
}

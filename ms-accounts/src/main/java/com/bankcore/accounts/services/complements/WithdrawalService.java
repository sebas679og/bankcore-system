package com.bankcore.accounts.services.complements;

import com.bankcore.accounts.config.DailyWithdrawalLimit;
import com.bankcore.accounts.utils.enums.AccountType;
import java.math.BigDecimal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service responsible for resolving the daily withdrawal limit based on the configured account
 * type.
 *
 * <p>This service queries the {@link DailyWithdrawalLimit} configuration to determine the maximum
 * withdrawal amount allowed per day depending on the {@link AccountType}.
 *
 * <p>If no limit is configured for the requested account type, an {@link IllegalArgumentException}
 * will be thrown.
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 0.1.0
 * @see DailyWithdrawalLimit
 * @see AccountType
 */
@RequiredArgsConstructor
@Service
public class WithdrawalService {

  private final DailyWithdrawalLimit withdrawalLimit;

  /**
   * Resolves the daily withdrawal limit for the specified account type.
   *
   * @param type the account type for which to resolve the withdrawal limit
   * @return the daily withdrawal limit as a {@link BigDecimal}
   * @throws IllegalArgumentException if no withdrawal limit is configured for the given account
   *     type
   */
  public BigDecimal resolveDailyLimit(AccountType type) {
    Map<AccountType, BigDecimal> limits = withdrawalLimit.getLimits();

    BigDecimal limit = limits.get(type);

    if (limit == null) {
      throw new IllegalArgumentException("No withdrawal limit configured for account type");
    }

    return limit;
  }
}

package com.bankcore.accounts.config;

import com.bankcore.accounts.utils.enums.AccountType;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Utility class to determine the daily withdrawal limit based on the account type.
 *
 * @author BankCore Team - Sebastian Orjuela
 * @version 0.1.0
 */
@Component
@ConfigurationProperties(prefix = "accounts.withdrawal")
public class DailyWithdrawalLimit {

  private Map<AccountType, BigDecimal> limits = new EnumMap<>(AccountType.class);

  public Map<AccountType, BigDecimal> getLimits() {
    return limits;
  }

  /**
   * Sets the daily withdrawal limits for each account type.
   *
   * @param limits a map containing the account type and its corresponding withdrawal limit
   * @throws IllegalArgumentException if any limit is null or less than or equal to zero
   */
  public void setLimits(Map<AccountType, BigDecimal> limits) {

    limits.forEach(
        (type, value) -> {
          if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Withdrawal limit must be greater than zero for account type: " + type);
          }
        });

    this.limits = limits;
  }
}

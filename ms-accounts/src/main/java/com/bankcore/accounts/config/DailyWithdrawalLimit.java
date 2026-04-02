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
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "accounts.withdrawal")
public class DailyWithdrawalLimit {

  private Map<AccountType, BigDecimal> limits = new EnumMap<>(AccountType.class);

  public Map<AccountType, BigDecimal> getLimits() {
    return limits;
  }

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

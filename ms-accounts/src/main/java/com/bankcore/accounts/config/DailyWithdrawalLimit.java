package com.bankcore.accounts.config;

import com.bankcore.accounts.utils.enums.AccountType;
import java.math.BigDecimal;
import java.util.Collections;
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

  // Using a synchronized map to ensure thread safety when accessing and modifying limits
  private final Map<AccountType, BigDecimal> limits =
      Collections.synchronizedMap(new EnumMap<>(AccountType.class));

  /**
   * Getter method to retrieve the current withdrawal limits for each account type. This method
   * returns a copy of the internal limits map to prevent external modification and ensure thread.
   *
   * @return a map containing the current withdrawal limits for each account type
   */
  public Map<AccountType, BigDecimal> getLimits() {
    synchronized (limits) {
      return new EnumMap<>(limits);
    }
  }

  /**
   * Setter method to update the withdrawal limits for each account type. This method validates that
   * the provided limits are positive values before updating the internal map.
   *
   * @param newLimits a map containing the new withdrawal limits for each account type
   * @throws IllegalArgumentException if any of the provided limits are null or not greater than
   *     zero
   */
  public void setLimits(Map<AccountType, BigDecimal> newLimits) {
    newLimits.forEach(
        (type, value) -> {
          if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Withdrawal limit must be greater than zero for account type: " + type);
          }
        });

    // Update the limits map in a thread-safe manner
    synchronized (limits) {
      limits.clear();
      limits.putAll(newLimits);
    }
  }
}

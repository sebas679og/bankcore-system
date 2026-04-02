package com.bankcore.accounts.services.complements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.bankcore.accounts.config.DailyWithdrawalLimit;
import com.bankcore.accounts.utils.enums.AccountType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WithdrawalServiceTest {

  @Mock private DailyWithdrawalLimit withdrawalLimit;

  @InjectMocks private WithdrawalService withdrawalService;

  @Test
  void shouldReturnLimitWhenAccountTypeExists() {

    Map<AccountType, BigDecimal> limits = new HashMap<>();
    limits.put(AccountType.SAVINGS, new BigDecimal("1000"));

    when(withdrawalLimit.getLimits()).thenReturn(limits);

    BigDecimal result = withdrawalService.resolveDailyLimit(AccountType.SAVINGS);

    assertEquals(new BigDecimal("1000"), result);
  }

  @Test
  void shouldThrowExceptionWhenAccountTypeNotConfigured() {

    Map<AccountType, BigDecimal> limits = new HashMap<>();

    when(withdrawalLimit.getLimits()).thenReturn(limits);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> withdrawalService.resolveDailyLimit(AccountType.SAVINGS));

    assertEquals("No withdrawal limit configured for account type", exception.getMessage());
  }
}

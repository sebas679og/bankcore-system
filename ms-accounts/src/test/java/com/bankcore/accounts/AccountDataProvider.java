package com.bankcore.accounts;

import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.models.AccountPinSecurity;
import com.bankcore.accounts.services.complements.IbanGeneratorService;
import com.bankcore.accounts.utils.enums.AccountStatus;
import com.bankcore.accounts.utils.enums.AccountType;
import com.bankcore.accounts.utils.enums.CurrencyCode;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Utility class that provides mock {@link AccountEntity} instances for testing purposes.
 *
 * <p>This class centralizes the creation of sample accounts with configurable attributes such as
 * customer ID, alias, balance, and status. It ensures consistency across tests by reusing a single
 * {@link IbanGeneratorService} to generate valid Spanish IBANs.
 *
 * <p>Typical usage includes simulating active or inactive accounts, accounts with custom balances,
 * or accounts tied to specific customers. By abstracting account creation, it reduces boilerplate
 * in test cases and improves readability.
 *
 * @author BankcoreTeam
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
public class AccountDataProvider {

  private static final IbanGeneratorService generatorService = new IbanGeneratorService();

  public static final String CUSTOMER_TEST_UUID = "e7c6be34-c77b-4afa-aebb-327354a9fe0b";
  public static final String INVALID_IBAN = "ES2553907030769590566957";

  /**
   * Creates a mock {@link AccountEntity} with default attributes.
   *
   * <p>The account is initialized as a savings account in EUR currency, with zero balance, active
   * status, and a daily withdrawal limit. A random customer ID and valid Spanish IBAN are
   * generated.
   *
   * @return a fully initialized mock {@link AccountEntity}
   */
  public static AccountEntity createMockAccount() {
    AccountEntity account =
        AccountEntity.builder()
            .accountNumber(generatorService.generateSpanishIban())
            .customerId(UUID.randomUUID())
            .accountType(AccountType.SAVINGS)
            .currency(CurrencyCode.EUR)
            .balance(BigDecimal.ZERO)
            .alias("mock-account")
            .status(AccountStatus.ACTIVE)
            .dailyWithdrawalLimit(BigDecimal.valueOf(1000))
            .security(AccountPinSecurity.builder().build())
            .build();

    account.getSecurity().setAccount(account);
    return account;
  }

  /**
   * Creates a mock {@link AccountEntity} tied to a specific customer.
   *
   * <p>This variant allows setting a custom customer ID and alias, while maintaining default
   * attributes such as savings type, EUR currency, active status, and daily withdrawal limit.
   *
   * @param customerId the UUID of the customer to associate
   * @param alias the alias to assign to the account
   * @return a mock {@link AccountEntity} linked to the given customer
   */
  public static AccountEntity createMockAccount(UUID customerId, String alias) {
    AccountEntity account =
        AccountEntity.builder()
            .accountNumber(generatorService.generateSpanishIban())
            .customerId(customerId)
            .accountType(AccountType.SAVINGS)
            .currency(CurrencyCode.EUR)
            .balance(BigDecimal.ZERO)
            .alias(alias)
            .status(AccountStatus.ACTIVE)
            .dailyWithdrawalLimit(BigDecimal.valueOf(1000))
            .security(AccountPinSecurity.builder().build())
            .build();

    account.getSecurity().setAccount(account);
    return account;
  }

  /**
   * Creates a mock {@link AccountEntity} with a custom initial balance.
   *
   * <p>Useful for simulating accounts with non-zero funds in test scenarios. Other attributes such
   * as account type, currency, and status remain default.
   *
   * @param initialBalance the starting balance for the account
   * @return a mock {@link AccountEntity} initialized with the given balance
   */
  public static AccountEntity createMockAccount(BigDecimal initialBalance) {
    AccountEntity account =
        AccountEntity.builder()
            .accountNumber(generatorService.generateSpanishIban())
            .customerId(UUID.randomUUID())
            .accountType(AccountType.SAVINGS)
            .currency(CurrencyCode.EUR)
            .balance(initialBalance)
            .alias("mock-account")
            .status(AccountStatus.ACTIVE)
            .dailyWithdrawalLimit(BigDecimal.valueOf(1000))
            .security(AccountPinSecurity.builder().build())
            .build();

    account.getSecurity().setAccount(account);
    return account;
  }

  /**
   * Creates a mock {@link AccountEntity} with inactive status.
   *
   * <p>This variant is intended for testing scenarios where account operations must fail or be
   * restricted due to inactivity.
   *
   * @return a mock {@link AccountEntity} marked as inactive
   */
  public static AccountEntity createMockAccountStatusInactive() {
    AccountEntity account =
        AccountEntity.builder()
            .accountNumber(generatorService.generateSpanishIban())
            .customerId(UUID.randomUUID())
            .accountType(AccountType.SAVINGS)
            .currency(CurrencyCode.EUR)
            .balance(BigDecimal.ZERO)
            .alias("mock-account")
            .status(AccountStatus.INACTIVE)
            .dailyWithdrawalLimit(BigDecimal.valueOf(1000))
            .security(AccountPinSecurity.builder().build())
            .build();

    account.getSecurity().setAccount(account);
    return account;
  }

  /**
   * Generates a valid Spanish IBAN using {@link IbanGeneratorService}.
   *
   * <p>This method provides a reusable way to obtain test IBANs without creating a full account
   * entity.
   *
   * @return a randomly generated Spanish IBAN string
   */
  public static String generateIban() {
    return generatorService.generateSpanishIban();
  }
}

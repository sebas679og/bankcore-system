package com.bankcore.accounts.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bankcore.accounts.AbstractIntegrationTest;
import com.bankcore.accounts.AccountDataProvider;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.models.AccountPinSecurity;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Integration tests for verification of data access methods. */
public class AccountPinSecurityRepositoryTest extends AbstractIntegrationTest {

  @Autowired AccountPinSecurityRepository accountPinSecurityRepository;

  @Autowired AccountRepository accountRepository;

  @BeforeEach
  void setUp() {
    accountRepository.deleteAll();
  }

  @Test
  void shouldFindPinSecurityByAccountId() {
    AccountEntity account = accountRepository.save(AccountDataProvider.createMockAccount());

    Optional<AccountPinSecurity> result =
        accountPinSecurityRepository.findByAccountId(account.getId());

    assertTrue(result.isPresent());
    assertEquals(account.getId(), result.get().getAccount().getId());
  }

  @Test
  void shouldReturnEmptyWhenAccountHasNoPinSecurity() {
    Optional<AccountPinSecurity> result =
        accountPinSecurityRepository.findByAccountId(UUID.randomUUID());

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldNotReturnPinSecurityOfAnotherAccount() {
    AccountEntity accountA = accountRepository.save(AccountDataProvider.createMockAccount());
    AccountEntity accountB = accountRepository.save(AccountDataProvider.createMockAccount());

    Optional<AccountPinSecurity> result =
        accountPinSecurityRepository.findByAccountId(accountA.getId());

    assertTrue(result.isPresent());
    assertNotEquals(accountB.getId(), result.get().getAccount().getId());
  }
}

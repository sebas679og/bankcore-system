package com.bankcore.accounts.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bankcore.accounts.AbstractIntegrationTest;
import com.bankcore.accounts.AccountDataProvider;
import com.bankcore.accounts.TransactionDataProvider;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.models.TransactionEntity;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TransactionRepositoryTest extends AbstractIntegrationTest {

  @Autowired TransactionRepository transactionRepository;

  @Autowired AccountRepository accountRepository;

  @BeforeEach
  void setUp() {
    transactionRepository.deleteAll();
    accountRepository.deleteAll();
  }

  @Test
  void shouldReturnLatestTransactionByAccountId() {
    AccountEntity account = accountRepository.save(AccountDataProvider.createMockAccount());

    TransactionEntity older =
        TransactionDataProvider.createMockTransaction(account, Instant.now().minusSeconds(60));
    TransactionEntity newer = TransactionDataProvider.createMockTransaction(account, Instant.now());

    transactionRepository.save(older);
    transactionRepository.save(newer);

    Optional<TransactionEntity> result =
        transactionRepository.findTopByAccount_IdOrderByCreatedAtDesc(account.getId());

    assertTrue(result.isPresent());
    assertEquals(newer.getId(), result.get().getId());
  }

  @Test
  void shouldReturnEmptyWhenNoTransactionsExistForAccount() {
    AccountEntity account = accountRepository.save(AccountDataProvider.createMockAccount());

    Optional<TransactionEntity> result =
        transactionRepository.findTopByAccount_IdOrderByCreatedAtDesc(account.getId());

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnOnlyTransactionsOfGivenAccount() {
    AccountEntity accountA = accountRepository.save(AccountDataProvider.createMockAccount());
    AccountEntity accountB = accountRepository.save(AccountDataProvider.createMockAccount());

    TransactionEntity txA =
        TransactionDataProvider.createMockTransaction(accountA, Instant.now().minusSeconds(30));
    TransactionEntity txB = TransactionDataProvider.createMockTransaction(accountB, Instant.now());

    transactionRepository.save(txA);
    transactionRepository.save(txB);

    Optional<TransactionEntity> result =
        transactionRepository.findTopByAccount_IdOrderByCreatedAtDesc(accountA.getId());

    assertTrue(result.isPresent());
    assertEquals(txA.getId(), result.get().getId());
  }
}

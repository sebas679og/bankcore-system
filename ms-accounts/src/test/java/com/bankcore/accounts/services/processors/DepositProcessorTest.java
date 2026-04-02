package com.bankcore.accounts.services.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bankcore.accounts.dto.responses.TransactionResponse;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.models.TransactionEntity;
import com.bankcore.accounts.repositories.AccountRepository;
import com.bankcore.accounts.repositories.TransactionRepository;
import com.bankcore.accounts.utils.enums.TransactionStatus;
import com.bankcore.accounts.utils.enums.TransactionType;
import com.bankcore.accounts.utils.mappers.TransactionMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DepositProcessorTest {

  @Mock private AccountRepository accountRepository;

  @Mock private TransactionRepository transactionRepository;

  @Mock private TransactionMapper transactionMapper;

  @InjectMocks private DepositProcessor depositProcessor;

  @Test
  void shouldProcessDepositSuccessfully() {
    AccountEntity account = new AccountEntity();
    account.setBalance(new BigDecimal("100.00"));

    BigDecimal amount = new BigDecimal("50.00");
    BigDecimal expectedBalance = new BigDecimal("150.00");

    TransactionResponse response = TransactionResponse.builder().build();

    when(transactionMapper.toTransactionResponse(any(), any())).thenReturn(response);

    TransactionResponse result = depositProcessor.processDeposit(account, amount, "Test deposit");

    assertNotNull(result);
    assertEquals(expectedBalance, account.getBalance());

    verify(transactionRepository).save(any(TransactionEntity.class));
    verify(accountRepository).save(account);
    verify(transactionMapper).toTransactionResponse(any(), eq(new BigDecimal("100.00")));
  }

  @Test
  void shouldCreateCorrectTransactionEntity() {
    AccountEntity account = new AccountEntity();
    account.setBalance(new BigDecimal("200.00"));

    BigDecimal amount = new BigDecimal("50.00");

    ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);

    when(transactionMapper.toTransactionResponse(any(), any()))
        .thenReturn(TransactionResponse.builder().build());

    depositProcessor.processDeposit(account, amount, "Deposit test");

    verify(transactionRepository).save(captor.capture());

    TransactionEntity savedTransaction = captor.getValue();

    assertEquals(TransactionType.DEPOSIT, savedTransaction.getType());
    assertEquals(amount, savedTransaction.getAmount());
    assertEquals(new BigDecimal("250.00"), savedTransaction.getBalanceAfter());
    assertEquals(TransactionStatus.COMPLETED, savedTransaction.getStatus());
    assertEquals("Deposit test", savedTransaction.getDescription());
  }

  @Test
  void shouldSendCorrectBalanceBeforeToMapper() {
    AccountEntity account = new AccountEntity();
    account.setBalance(new BigDecimal("300.00"));

    ArgumentCaptor<BigDecimal> balanceCaptor = ArgumentCaptor.forClass(BigDecimal.class);

    when(transactionMapper.toTransactionResponse(any(), any()))
        .thenReturn(TransactionResponse.builder().build());

    depositProcessor.processDeposit(account, new BigDecimal("100"), "test");

    verify(transactionMapper).toTransactionResponse(any(), balanceCaptor.capture());

    assertEquals(new BigDecimal("300.00"), balanceCaptor.getValue());
  }

  @Test
  void shouldHandleZeroAmountDeposit() {
    AccountEntity account = new AccountEntity();
    account.setBalance(new BigDecimal("100.00"));

    when(transactionMapper.toTransactionResponse(any(), any()))
        .thenReturn(TransactionResponse.builder().build());

    depositProcessor.processDeposit(account, BigDecimal.ZERO, "zero");

    assertEquals(new BigDecimal("100.00"), account.getBalance());
  }
}

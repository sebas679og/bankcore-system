package com.bankcore.accounts.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.bankcore.accounts.dto.requests.TransactionQueryParams;
import com.bankcore.accounts.dto.requests.TransactionRequest;
import com.bankcore.accounts.dto.requests.TransferRequest;
import com.bankcore.accounts.dto.responses.TransactionResponse;
import com.bankcore.accounts.dto.responses.TransactionsHistoryResponse;
import com.bankcore.accounts.dto.responses.TransferResponse;
import com.bankcore.accounts.exceptions.BusinessException;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.services.complements.CustomerAccountValidator;
import com.bankcore.accounts.services.processors.DepositProcessor;
import com.bankcore.accounts.services.processors.TransactionHistoryProcessor;
import com.bankcore.accounts.services.processors.TransferProcessor;
import com.bankcore.accounts.services.processors.WithdrawalProcessor;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

  @Mock private CustomerAccountValidator validator;

  @Mock private DepositProcessor depositProcessor;

  @Mock private WithdrawalProcessor withdrawalProcessor;

  @Mock private TransferProcessor transferProcessor;

  @Mock private TransactionHistoryProcessor transactionHistoryProcessor;

  @InjectMocks private TransactionServiceImpl transactionService;

  private UUID customerId;
  private UUID accountId;

  @BeforeEach
  public void setup() {
    customerId = UUID.randomUUID();
    accountId = UUID.randomUUID();
  }

  @Test
  void shouldReturnTransactionResponse_whenDepositSuccessful() {
    TransactionRequest request =
        TransactionRequest.builder()
            .amount(new BigDecimal("100.00"))
            .pin("1234")
            .description("Transaction test")
            .build();

    AccountEntity account = new AccountEntity();
    TransactionResponse response = TransactionResponse.builder().build();

    when(validator.validateCustomerAccountAndPin(customerId, accountId, "1234"))
        .thenReturn(account);
    when(depositProcessor.processDeposit(account, request.getAmount(), request.getDescription()))
        .thenReturn(response);

    TransactionResponse result = transactionService.makeDeposit(request, accountId, customerId);

    assertNotNull(result);
    assertEquals(response, result);

    verify(validator).validateCustomerAccountAndPin(customerId, accountId, "1234");
    verify(depositProcessor).processDeposit(account, request.getAmount(), request.getDescription());
  }

  @Test
  void shouldReturnTransferResponse_whenTransferSuccessful() {
    TransferRequest request =
        TransferRequest.builder()
            .sourceAccountId(accountId)
            .destinationAccountNumber("EXT123")
            .pin("1234")
            .amount(new BigDecimal("100"))
            .description("external transfer")
            .build();

    AccountEntity sourceAccount = new AccountEntity();
    TransferResponse response = TransferResponse.builder().build();

    when(validator.validateCustomerAccountAndPin(customerId, accountId, "1234"))
        .thenReturn(sourceAccount);
    when(transferProcessor.processTransfer(sourceAccount, request)).thenReturn(response);

    TransferResponse result = transactionService.makeTransfer(request, customerId);

    assertNotNull(result);
    assertEquals(response, result);

    verify(validator).validateCustomerAccountAndPin(customerId, accountId, "1234");
    verify(transferProcessor).processTransfer(sourceAccount, request);
  }

  @Test
  void shouldThrowException_whenValidatorFailsOnDeposit() {
    TransactionRequest request =
        TransactionRequest.builder()
            .amount(new BigDecimal("100.00"))
            .pin("1234")
            .description("Deposit test")
            .build();

    when(validator.validateCustomerAccountAndPin(customerId, accountId, "1234"))
        .thenThrow(new RuntimeException("Customer invalid"));

    assertThrows(
        RuntimeException.class,
        () -> transactionService.makeDeposit(request, accountId, customerId));

    verify(depositProcessor, never()).processDeposit(any(), any(), any());
  }

  @Test
  void shouldReturnTransactionResponse_whenWithdrawalSuccessful() {
    TransactionRequest request =
        TransactionRequest.builder()
            .amount(new BigDecimal("100.00"))
            .pin("1234")
            .description("Withdrawal test")
            .build();

    AccountEntity account = new AccountEntity();
    account.setBalance(new BigDecimal("500.00"));
    TransactionResponse response = TransactionResponse.builder().build();

    when(validator.validateCustomerAccountAndPin(customerId, accountId, "1234"))
        .thenReturn(account);
    when(withdrawalProcessor.processWithdrawal(
            account, request.getAmount(), request.getDescription()))
        .thenReturn(response);

    TransactionResponse result = transactionService.makeWithdrawal(request, accountId, customerId);

    assertNotNull(result);
    assertEquals(response, result);

    verify(validator).validateCustomerAccountAndPin(customerId, accountId, "1234");
    verify(withdrawalProcessor)
        .processWithdrawal(account, request.getAmount(), request.getDescription());
  }

  @Test
  void shouldThrowBusinessException_whenWithdrawalHasInsufficientFunds() {
    TransactionRequest request =
        TransactionRequest.builder()
            .amount(new BigDecimal("1000.00"))
            .pin("1234")
            .description("Withdrawal test")
            .build();

    AccountEntity account = new AccountEntity();
    account.setBalance(new BigDecimal("500.00"));

    when(validator.validateCustomerAccountAndPin(customerId, accountId, "1234"))
        .thenReturn(account);

    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> transactionService.makeWithdrawal(request, accountId, customerId));

    assertEquals("INSUFFICIENT_FUNDS", exception.getMessage());
    verify(withdrawalProcessor, never()).processWithdrawal(any(), any(), any());
  }

  @Test
  void shouldThrowException_whenValidatorFailsOnWithdrawal() {
    TransactionRequest request =
        TransactionRequest.builder()
            .amount(new BigDecimal("100.00"))
            .pin("1234")
            .description("Withdrawal test")
            .build();

    when(validator.validateCustomerAccountAndPin(customerId, accountId, "1234"))
        .thenThrow(new RuntimeException("PIN invalid"));

    assertThrows(
        RuntimeException.class,
        () -> transactionService.makeWithdrawal(request, accountId, customerId));

    verify(withdrawalProcessor, never()).processWithdrawal(any(), any(), any());
  }

  @Test
  void shouldDelegateAndReturnResult() {
    TransactionQueryParams filters = new TransactionQueryParams();
    TransactionsHistoryResponse expected = TransactionsHistoryResponse.builder().build();

    when(transactionHistoryProcessor.getTransactions(accountId, customerId, filters))
        .thenReturn(expected);

    TransactionsHistoryResponse result =
        transactionService.getTransactionsHistory(accountId, customerId, filters);

    assertThat(result).isSameAs(expected);
    verify(transactionHistoryProcessor).getTransactions(accountId, customerId, filters);
  }
}

package com.bankcore.accounts.services.processors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.bankcore.accounts.dto.requests.TransferRequest;
import com.bankcore.accounts.dto.responses.TransferResponse;
import com.bankcore.accounts.exceptions.AccountInactiveException;
import com.bankcore.accounts.exceptions.InsufficientBalanceException;
import com.bankcore.accounts.exceptions.InvalidTransferDestinationException;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.models.TransactionEntity;
import com.bankcore.accounts.repositories.AccountRepository;
import com.bankcore.accounts.repositories.TransactionRepository;
import com.bankcore.accounts.repositories.TransferRepository;
import com.bankcore.accounts.services.complements.CustomerValidationService;
import com.bankcore.accounts.utils.enums.AccountStatus;
import com.bankcore.accounts.utils.enums.TransactionType;
import com.bankcore.accounts.utils.enums.TransferStatus;
import com.bankcore.accounts.utils.mappers.TransactionMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TransferProcessorTest {

  @Mock private AccountRepository accountRepository;

  @Mock private TransferRepository transferRepository;

  @Mock private TransactionRepository transactionRepository;

  @Mock private CustomerValidationService validationService;

  @Mock private TransactionMapper transactionMapper;

  @InjectMocks private TransferProcessor transferProcessor;

  @Test
  void shouldProcessInternalTransferSuccessfully() {
    AccountEntity source = new AccountEntity();
    source.setBalance(new BigDecimal("500"));
    source.setAccountNumber("SRC123");
    source.setCustomerId(UUID.randomUUID());

    AccountEntity destination = new AccountEntity();
    destination.setBalance(new BigDecimal("200"));
    destination.setAccountNumber("DST123");
    destination.setCustomerId(UUID.randomUUID());
    destination.setStatus(AccountStatus.ACTIVE);

    TransferRequest request =
        TransferRequest.builder()
            .sourceAccountId(UUID.randomUUID())
            .destinationAccountNumber("DST123")
            .pin("1234")
            .amount(new BigDecimal("100"))
            .description("test transfer")
            .build();

    when(accountRepository.existsByAccountNumber("DST123")).thenReturn(true);
    when(accountRepository.findByAccountNumber("DST123")).thenReturn(Optional.of(destination));

    when(validationService.getFullNameCustomerById(any())).thenReturn("User Name");

    when(transactionMapper.toTransferResponse(any(), any(), any(), any()))
        .thenReturn(TransferResponse.builder().build());

    TransferResponse result = transferProcessor.processTransfer(source, request);

    assertNotNull(result);
    assertEquals(new BigDecimal("400"), source.getBalance());
    assertEquals(new BigDecimal("300"), destination.getBalance());

    verify(transferRepository).save(any());
    verify(transactionRepository, times(2)).save(any()); // OUT + IN
    verify(accountRepository, times(2)).save(any());
  }

  @Test
  void shouldProcessExternalTransfer_whenDestinationDoesNotExist() {
    AccountEntity source = new AccountEntity();
    source.setBalance(new BigDecimal("500"));
    source.setAccountNumber("SRC123");

    TransferRequest request =
        TransferRequest.builder()
            .sourceAccountId(UUID.randomUUID())
            .destinationAccountNumber("EXT123")
            .pin("1234")
            .amount(new BigDecimal("100"))
            .description("external transfer")
            .build();

    when(accountRepository.existsByAccountNumber("EXT123")).thenReturn(false);

    when(transactionMapper.toTransferResponse(any(), any(), any(), any()))
        .thenReturn(TransferResponse.builder().status(TransferStatus.PENDING).build());

    TransferResponse result = transferProcessor.processTransfer(source, request);

    assertNotNull(result);
    assertEquals(new BigDecimal("400"), source.getBalance());
    assertEquals(TransferStatus.PENDING, result.getStatus());

    verify(transactionRepository, times(1)).save(any()); // solo OUT
    verify(accountRepository, times(1)).save(source);
  }

  @Test
  void shouldThrowInvalidTransferDestinationException_whenSourceAndDestinationAccountAreTheSame() {
    AccountEntity source = new AccountEntity();
    source.setBalance(new BigDecimal("500"));
    source.setAccountNumber("SRC123");

    TransferRequest request =
        TransferRequest.builder()
            .sourceAccountId(UUID.randomUUID())
            .destinationAccountNumber("SRC123")
            .pin("1234")
            .amount(new BigDecimal("100"))
            .description("same account transfer")
            .build();

    assertThrows(
        InvalidTransferDestinationException.class,
        () -> transferProcessor.processTransfer(source, request));
  }

  @Test
  void shouldThrowException_whenInsufficientBalance() {
    AccountEntity source = new AccountEntity();
    source.setBalance(new BigDecimal("50"));

    TransferRequest request =
        TransferRequest.builder()
            .sourceAccountId(UUID.randomUUID())
            .destinationAccountNumber("DST123")
            .pin("1234")
            .amount(new BigDecimal("100"))
            .description("fail")
            .build();

    when(accountRepository.existsByAccountNumber("DST123")).thenReturn(false);

    assertThrows(
        InsufficientBalanceException.class,
        () -> transferProcessor.processTransfer(source, request));

    verify(transferRepository, never()).save(any());
  }

  @Test
  void shouldThrowException_whenDestinationAccountInactive() {
    AccountEntity destination = new AccountEntity();
    destination.setStatus(AccountStatus.INACTIVE);

    when(accountRepository.existsByAccountNumber("DST123")).thenReturn(true);

    when(accountRepository.findByAccountNumber("DST123")).thenReturn(Optional.of(destination));

    TransferRequest request =
        TransferRequest.builder()
            .sourceAccountId(UUID.randomUUID())
            .destinationAccountNumber("DST123")
            .pin("1234")
            .amount(new BigDecimal("100"))
            .description("fail")
            .build();

    AccountEntity source = new AccountEntity();
    source.setBalance(new BigDecimal("500"));

    assertThrows(
        AccountInactiveException.class, () -> transferProcessor.processTransfer(source, request));
  }

  @Test
  void shouldCreateCorrectTransactionTypes() {
    AccountEntity source = new AccountEntity();
    source.setBalance(new BigDecimal("500"));
    source.setAccountNumber("SRC123");

    AccountEntity destination = new AccountEntity();
    destination.setBalance(new BigDecimal("200"));
    destination.setAccountNumber("DST123");
    destination.setStatus(AccountStatus.ACTIVE);

    when(accountRepository.existsByAccountNumber("DST123")).thenReturn(true);
    when(accountRepository.findByAccountNumber("DST123")).thenReturn(Optional.of(destination));

    when(validationService.getFullNameCustomerById(any())).thenReturn("User");

    when(transactionMapper.toTransferResponse(any(), any(), any(), any()))
        .thenReturn(TransferResponse.builder().build());

    ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);

    transferProcessor.processTransfer(
        source,
        TransferRequest.builder()
            .sourceAccountId(UUID.randomUUID())
            .destinationAccountNumber("DST123")
            .pin("1234")
            .amount(new BigDecimal("100"))
            .description("fail")
            .build());

    verify(transactionRepository, times(2)).save(captor.capture());

    List<TransactionEntity> txs = captor.getAllValues();

    assertTrue(txs.stream().anyMatch(t -> t.getType() == TransactionType.TRANSFER_OUT));
    assertTrue(txs.stream().anyMatch(t -> t.getType() == TransactionType.TRANSFER_IN));
  }
}

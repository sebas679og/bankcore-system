package com.bankcore.accounts.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.bankcore.accounts.AccountDataProvider;
import com.bankcore.accounts.dto.requests.AccountRegisterRequest;
import com.bankcore.accounts.dto.responses.AccountRegisterResponse;
import com.bankcore.accounts.dto.responses.UserAccountDetailResponse;
import com.bankcore.accounts.dto.responses.UserAccountResponse;
import com.bankcore.accounts.exceptions.AccountNotFoundException;
import com.bankcore.accounts.exceptions.BusinessException;
import com.bankcore.accounts.exceptions.ResourceConflictException;
import com.bankcore.accounts.integrations.client.CustomerClient;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.models.TransactionEntity;
import com.bankcore.accounts.repositories.AccountRepository;
import com.bankcore.accounts.repositories.TransactionRepository;
import com.bankcore.accounts.services.complements.CustomerValidationService;
import com.bankcore.accounts.services.complements.IbanGeneratorService;
import com.bankcore.accounts.services.complements.WithdrawalService;
import com.bankcore.accounts.utils.enums.AccountType;
import com.bankcore.accounts.utils.enums.CurrencyCode;
import com.bankcore.accounts.utils.mappers.AccountMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link AccountManagementImpl} covering account registration and retrieval of user.
 */
@ExtendWith(MockitoExtension.class)
public class AccountManagementImplTest {

  @Mock private CustomerClient customerClient;

  @Mock private AccountRepository accountRepository;

  @Mock private TransactionRepository transactionRepository;

  @Mock private IbanGeneratorService ibanGeneratorService;

  @Mock private CustomerValidationService validationService;

  @Mock private WithdrawalService withdrawalService;

  @Mock private AccountMapper accountMapper;

  @InjectMocks private AccountManagementImpl accountManagement;

  private UUID customerId;
  private AccountRegisterRequest request;

  @BeforeEach
  void setUp() {
    customerId = UUID.randomUUID();

    request =
        AccountRegisterRequest.builder()
            .accountType(AccountType.SAVINGS)
            .alias("my-account")
            .currency(CurrencyCode.EUR)
            .build();
  }

  @Test
  void shouldThrowBusinessException_whenCustomerHasThreeAccounts() {

    doNothing().when(validationService).validateCustomerIsActive(customerId);

    when(accountRepository.countByCustomerId(customerId)).thenReturn(3L);

    assertThrows(
        BusinessException.class, () -> accountManagement.registerAccount(request, customerId));
  }

  @Test
  void shouldThrowConflict_whenAliasAlreadyExists() {

    doNothing().when(validationService).validateCustomerIsActive(customerId);

    when(accountRepository.countByCustomerId(customerId)).thenReturn(1L);

    when(accountRepository.existsByAliasAndCustomerId("my-account", customerId)).thenReturn(true);

    assertThrows(
        ResourceConflictException.class,
        () -> accountManagement.registerAccount(request, customerId));
  }

  @Test
  void shouldGenerateNewIban_ifGeneratedIbanAlreadyExists() {

    doNothing().when(validationService).validateCustomerIsActive(customerId);

    when(accountRepository.countByCustomerId(customerId)).thenReturn(0L);

    when(accountRepository.existsByAliasAndCustomerId(any(), any())).thenReturn(false);

    when(ibanGeneratorService.generateSpanishIban()).thenReturn("IBAN1").thenReturn("IBAN2");

    when(accountRepository.existsByAccountNumber("IBAN1")).thenReturn(true);

    when(accountRepository.existsByAccountNumber("IBAN2")).thenReturn(false);

    when(withdrawalService.resolveDailyLimit(any())).thenReturn(BigDecimal.valueOf(1000));

    when(accountMapper.toAccountRegisterResponse(any()))
        .thenReturn(AccountRegisterResponse.builder().accountNumber("IBAN2").build());

    AccountRegisterResponse response = accountManagement.registerAccount(request, customerId);

    assertEquals("IBAN2", response.getAccountNumber());
  }

  @Test
  void shouldRegisterAccountSuccessfully() {

    doNothing().when(validationService).validateCustomerIsActive(customerId);

    when(accountRepository.countByCustomerId(customerId)).thenReturn(0L);

    when(accountRepository.existsByAliasAndCustomerId(any(), any())).thenReturn(false);

    when(ibanGeneratorService.generateSpanishIban()).thenReturn("VALID_IBAN");

    when(accountRepository.existsByAccountNumber("VALID_IBAN")).thenReturn(false);

    when(withdrawalService.resolveDailyLimit(any())).thenReturn(BigDecimal.valueOf(1000));

    AccountRegisterResponse mappedResponse =
        AccountRegisterResponse.builder().accountNumber("VALID_IBAN").build();

    when(accountMapper.toAccountRegisterResponse(any())).thenReturn(mappedResponse);

    AccountRegisterResponse response = accountManagement.registerAccount(request, customerId);

    assertNotNull(response);
    assertEquals("VALID_IBAN", response.getAccountNumber());

    verify(accountRepository).save(any(AccountEntity.class));
  }

  @Test
  void shouldReturnMappedAccounts_whenCustomerExistsAndHasAccounts() {
    List<AccountEntity> entities = List.of(mock(AccountEntity.class), mock(AccountEntity.class));
    List<UserAccountResponse> expected =
        List.of(mock(UserAccountResponse.class), mock(UserAccountResponse.class));

    when(accountRepository.findAllByCustomerId(customerId)).thenReturn(entities);
    when(accountMapper.toResponseList(entities)).thenReturn(expected);

    List<UserAccountResponse> result = accountManagement.getCurrentUserAccounts(customerId);

    assertThat(result).isNotNull().hasSize(2).isEqualTo(expected);
  }

  @Test
  void shouldReturnEmptyList_whenCustomerExistsButHasNoAccounts() {
    when(accountRepository.findAllByCustomerId(customerId)).thenReturn(List.of());
    when(accountMapper.toResponseList(List.of())).thenReturn(List.of());

    List<UserAccountResponse> result = accountManagement.getCurrentUserAccounts(customerId);

    assertThat(result).isNotNull().isEmpty();
  }

  @Test
  void shouldThrowException_whenIdIsNull() {
    assertThatThrownBy(() -> accountManagement.getCurrentUserAccounts(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowIllegalArgumentException_whenIdIsNotValidUuid() {
    assertThatThrownBy(
            () -> accountManagement.getCurrentUserAccounts(UUID.fromString("not-a-uuid")))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowException_whenIdIsBlank() {
    assertThatThrownBy(() -> accountManagement.getCurrentUserAccounts(UUID.fromString(" ")))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldAlwaysCallClientBeforeRepository() {

    when(accountRepository.findAllByCustomerId(customerId)).thenReturn(List.of());
    when(accountMapper.toResponseList(any())).thenReturn(List.of());

    accountManagement.getCurrentUserAccounts(customerId);

    verify(accountRepository).findAllByCustomerId(customerId);
  }

  @Test
  void shouldPassRepositoryResultDirectlyToMapper() {
    List<AccountEntity> entities = List.of(mock(AccountEntity.class));

    when(accountRepository.findAllByCustomerId(customerId)).thenReturn(entities);
    when(accountMapper.toResponseList(entities))
        .thenReturn(List.of(mock(UserAccountResponse.class)));

    accountManagement.getCurrentUserAccounts(customerId);

    verify(accountMapper).toResponseList(entities);
  }

  @Test
  void shouldNeverCallMapper_whenRepositoryFails() {

    when(accountRepository.findAllByCustomerId(customerId))
        .thenThrow(new RuntimeException("DB error"));

    assertThatThrownBy(() -> accountManagement.getCurrentUserAccounts(customerId))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("DB error");

    verifyNoInteractions(accountMapper);
  }

  @Test
  void shouldReturnNullLastTxAt_whenNoTx() {

    AccountEntity mockAccount = AccountDataProvider.createMockAccount(customerId, "Some Alias");
    UUID accountId = mockAccount.getId();

    UserAccountDetailResponse detailResponse =
        UserAccountDetailResponse.builder()
            .id(mockAccount.getId())
            .accountNumber(mockAccount.getAccountNumber())
            .customerId(mockAccount.getCustomerId())
            .accountType(mockAccount.getAccountType())
            .currency(String.valueOf(mockAccount.getCurrency()))
            .balance(mockAccount.getBalance())
            .alias(mockAccount.getAlias())
            .status(mockAccount.getStatus())
            .createdAt(mockAccount.getCreatedAt())
            .lastTransactionAt(null)
            .build();

    when(accountRepository.findByIdAndCustomerId(accountId, customerId))
        .thenReturn(Optional.of(mockAccount));
    when(transactionRepository.findTopByAccountIdOrderByCreatedAtDesc(accountId))
        .thenReturn(Optional.empty());
    when(accountMapper.toDetailResponse(mockAccount, null)).thenReturn(detailResponse);

    UserAccountDetailResponse result = accountManagement.getAccountDetails(accountId, customerId);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(accountId);
    assertThat(result.getAccountNumber()).isEqualTo(mockAccount.getAccountNumber());
    assertThat(result.getLastTransactionAt()).isNull();
    verify(accountRepository).findByIdAndCustomerId(accountId, customerId);
    verify(transactionRepository).findTopByAccountIdOrderByCreatedAtDesc(accountId);
    verify(accountMapper).toDetailResponse(mockAccount, null);
  }

  @Test
  void shouldReturnLastTxAt_whenHasTx() {

    AccountEntity mockAccount = AccountDataProvider.createMockAccount(customerId, "Some Alias");
    UUID accountId = mockAccount.getId();
    Instant lastTransactionAt = Instant.now();

    TransactionEntity mockTransaction =
        TransactionEntity.builder()
            .id(UUID.randomUUID())
            .account(mockAccount)
            .createdAt(lastTransactionAt)
            .build();

    UserAccountDetailResponse detailResponse =
        UserAccountDetailResponse.builder()
            .id(mockAccount.getId())
            .accountNumber(mockAccount.getAccountNumber())
            .customerId(mockAccount.getCustomerId())
            .accountType(mockAccount.getAccountType())
            .currency(String.valueOf(mockAccount.getCurrency()))
            .balance(mockAccount.getBalance())
            .alias(mockAccount.getAlias())
            .status(mockAccount.getStatus())
            .createdAt(mockAccount.getCreatedAt())
            .lastTransactionAt(lastTransactionAt)
            .build();

    when(accountRepository.findByIdAndCustomerId(accountId, customerId))
        .thenReturn(Optional.of(mockAccount));
    when(transactionRepository.findTopByAccountIdOrderByCreatedAtDesc(accountId))
        .thenReturn(Optional.of(mockTransaction));
    when(accountMapper.toDetailResponse(mockAccount, lastTransactionAt)).thenReturn(detailResponse);

    UserAccountDetailResponse result = accountManagement.getAccountDetails(accountId, customerId);

    assertThat(result).isNotNull();
    assertThat(result.getLastTransactionAt()).isEqualTo(lastTransactionAt);
    verify(accountRepository).findByIdAndCustomerId(accountId, customerId);
    verify(transactionRepository).findTopByAccountIdOrderByCreatedAtDesc(accountId);
    verify(accountMapper).toDetailResponse(mockAccount, lastTransactionAt);
  }

  @Test
  void shouldThrowAccNotFoundEx_whenNotFound() {

    AccountEntity mockAccount = AccountDataProvider.createMockAccount(customerId, "Some Alias");
    UUID accountId = mockAccount.getId();

    when(accountRepository.findByIdAndCustomerId(accountId, customerId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> accountManagement.getAccountDetails(accountId, customerId))
        .isInstanceOf(AccountNotFoundException.class);

    verify(transactionRepository, never()).findTopByAccountIdOrderByCreatedAtDesc(any());
    verify(accountMapper, never()).toDetailResponse(any(), any());
  }

  @Test
  void shouldThrowAccNotFoundEx_whenOtherCustomer() {

    AccountEntity mockAccount = AccountDataProvider.createMockAccount(customerId, "Some Alias");
    UUID accountId = mockAccount.getId();
    UUID anotherCustomerId = UUID.randomUUID();

    when(accountRepository.findByIdAndCustomerId(accountId, anotherCustomerId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> accountManagement.getAccountDetails(accountId, anotherCustomerId))
        .isInstanceOf(AccountNotFoundException.class);

    verify(transactionRepository, never()).findTopByAccountIdOrderByCreatedAtDesc(any());
    verify(accountMapper, never()).toDetailResponse(any(), any());
  }
}

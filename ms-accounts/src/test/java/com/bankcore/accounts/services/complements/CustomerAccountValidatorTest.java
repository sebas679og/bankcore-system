package com.bankcore.accounts.services.complements;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.bankcore.accounts.exceptions.AccountInactiveException;
import com.bankcore.accounts.exceptions.AccountNotFoundException;
import com.bankcore.accounts.integrations.dto.responses.PinValidateResponse;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.repositories.AccountRepository;
import com.bankcore.accounts.utils.enums.AccountStatus;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CustomerAccountValidatorTest {

  @Mock private CustomerValidationService validationService;

  @Mock private PinAttemptManagerService pinSecurityService;

  @Mock private AccountRepository accountRepository;

  @InjectMocks private CustomerAccountValidator validator;

  private UUID customerId;
  private UUID accountId;

  @Test
  void shouldReturnAccount_whenAllValidationsPass() {
    customerId = UUID.randomUUID();
    accountId = UUID.randomUUID();

    AccountEntity account = new AccountEntity();
    account.setId(accountId);
    account.setCustomerId(customerId);
    account.setStatus(AccountStatus.ACTIVE);

    PinValidateResponse pinResponse = new PinValidateResponse(true);

    when(accountRepository.findByIdAndCustomerId(accountId, customerId))
        .thenReturn(Optional.of(account));

    when(validationService.validateCustomerPin(eq(customerId), any())).thenReturn(pinResponse);

    AccountEntity result = validator.validateCustomerAccountAndPin(customerId, accountId, "1234");

    assertNotNull(result);
    assertEquals(accountId, result.getId());

    verify(validationService).validateCustomerIsActive(customerId);
    verify(pinSecurityService).checkPinLock(accountId);
    verify(pinSecurityService).processPinAttempt(accountId, pinResponse);
  }

  @Test
  void shouldThrowAccountNotFound_whenAccountDoesNotExist() {
    when(accountRepository.findByIdAndCustomerId(accountId, customerId))
        .thenReturn(Optional.empty());

    assertThrows(
        AccountNotFoundException.class,
        () -> validator.validateCustomerAccountAndPin(customerId, accountId, "1234"));
  }

  @Test
  void shouldThrowAccountInactive_whenAccountIsNotActive() {
    AccountEntity account = new AccountEntity();
    account.setStatus(AccountStatus.FROZEN);

    when(accountRepository.findByIdAndCustomerId(accountId, customerId))
        .thenReturn(Optional.of(account));

    assertThrows(
        AccountInactiveException.class,
        () -> validator.validateCustomerAccountAndPin(customerId, accountId, "1234"));
  }

  @Test
  void shouldThrowException_whenPinIsLocked() {
    AccountEntity account = new AccountEntity();
    account.setStatus(AccountStatus.ACTIVE);

    when(accountRepository.findByIdAndCustomerId(accountId, customerId))
        .thenReturn(Optional.of(account));

    doThrow(new RuntimeException("PIN locked")).when(pinSecurityService).checkPinLock(accountId);

    assertThrows(
        RuntimeException.class,
        () -> validator.validateCustomerAccountAndPin(customerId, accountId, "1234"));
  }

  @Test
  void shouldThrowException_whenCustomerIsInactive() {
    doThrow(new RuntimeException("Customer inactive"))
        .when(validationService)
        .validateCustomerIsActive(customerId);

    assertThrows(
        RuntimeException.class,
        () -> validator.validateCustomerAccountAndPin(customerId, accountId, "1234"));
  }

  @Test
  void shouldProcessPinAttempt_whenPinIsInvalid() {
    AccountEntity account = new AccountEntity();
    account.setStatus(AccountStatus.ACTIVE);

    PinValidateResponse pinResponse = new PinValidateResponse(false);

    when(accountRepository.findByIdAndCustomerId(accountId, customerId))
        .thenReturn(Optional.of(account));

    when(validationService.validateCustomerPin(eq(customerId), any())).thenReturn(pinResponse);

    validator.validateCustomerAccountAndPin(customerId, accountId, "wrong");

    verify(pinSecurityService).processPinAttempt(accountId, pinResponse);
  }
}

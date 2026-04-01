package com.bankcore.accounts.services.complements;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.bankcore.accounts.exceptions.AccountPermanentlyLockedException;
import com.bankcore.accounts.exceptions.AccountTemporarilyLockedException;
import com.bankcore.accounts.exceptions.CustomInternalServiceException;
import com.bankcore.accounts.exceptions.IncorrectPinException;
import com.bankcore.accounts.integrations.dto.responses.PinValidateResponse;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.models.AccountPinSecurity;
import com.bankcore.accounts.repositories.AccountPinSecurityRepository;
import com.bankcore.accounts.utils.enums.AccountStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link PinAttemptManagerService} to verify correct handling of pin attempt logic.
 */
@ExtendWith(MockitoExtension.class)
public class PinAttemptMangerServiceTest {

  @Mock private AccountPinSecurityRepository accountPinSecurityRepository;

  @InjectMocks private PinAttemptManagerService service;

  private UUID accountId;
  private AccountPinSecurity pinSecurity;

  @BeforeEach
  void setUp() {
    AccountEntity account;
    accountId = UUID.randomUUID();
    account = new AccountEntity();
    account.setId(accountId);
    account.setStatus(AccountStatus.ACTIVE);

    pinSecurity = new AccountPinSecurity();
    pinSecurity.setAccount(account);
    pinSecurity.setFailedAttempts(0);
    pinSecurity.setPermanentLock(false);
    pinSecurity.setTemporaryLockUntil(null);

    when(accountPinSecurityRepository.findByAccount_Id(accountId))
        .thenReturn(Optional.of(pinSecurity));
  }

  @Test
  void shouldNotThrow_whenAccountIsNotLocked() {
    assertDoesNotThrow(() -> service.checkPinLock(accountId));
  }

  @Test
  void shouldThrowTemporarilyLocked_whenTempLockActive() {
    pinSecurity.setTemporaryLockUntil(Instant.now().plus(Duration.ofMinutes(5)));

    assertThrows(AccountTemporarilyLockedException.class, () -> service.checkPinLock(accountId));
  }

  @Test
  void shouldThrowPermanentlyLocked_whenPermanentLockActive() {
    pinSecurity.setPermanentLock(true);

    assertThrows(AccountPermanentlyLockedException.class, () -> service.checkPinLock(accountId));
  }

  @Test
  void shouldResetAttempts_whenPinValidAndNotClean() {
    pinSecurity.setFailedAttempts(2);

    PinValidateResponse response = new PinValidateResponse(true);

    service.processPinAttempt(accountId, response);

    assertEquals(0, pinSecurity.getFailedAttempts());
    assertNull(pinSecurity.getTemporaryLockUntil());
    assertFalse(pinSecurity.isPermanentLock());
  }

  @Test
  void shouldDoNothing_whenPinValidAndAlreadyClean() {
    PinValidateResponse response = new PinValidateResponse(true);

    service.processPinAttempt(accountId, response);

    assertEquals(0, pinSecurity.getFailedAttempts());
    assertNull(pinSecurity.getTemporaryLockUntil());
    assertFalse(pinSecurity.isPermanentLock());
  }

  @Test
  void shouldIncrementFailedAttempts_whenPinInvalidAndBelowThreshold() {
    assertEquals(1, pinSecurity.getFailedAttempts());
    assertNull(pinSecurity.getTemporaryLockUntil());
    assertFalse(pinSecurity.isPermanentLock());

    PinValidateResponse response = new PinValidateResponse(false);

    IncorrectPinException ex =
        assertThrows(
            IncorrectPinException.class, () -> service.processPinAttempt(accountId, response));

    assertTrue(
        ex.getMessage().contains(String.valueOf(PinAttemptManagerService.TEMP_LOCK_ATTEMPTS - 1)));
  }

  @Test
  void shouldTempLock_whenFailedAttemptsReachTempThreshold() {
    pinSecurity.setFailedAttempts(PinAttemptManagerService.TEMP_LOCK_ATTEMPTS - 1);

    PinValidateResponse response = new PinValidateResponse(false);

    assertThrows(
        AccountTemporarilyLockedException.class,
        () -> service.processPinAttempt(accountId, response));

    assertNotNull(pinSecurity.getTemporaryLockUntil());
    assertFalse(pinSecurity.isPermanentLock());
  }

  @Test
  void shouldPermanentLock_whenFailedAttemptsReachPermThreshold() {
    pinSecurity.setFailedAttempts(PinAttemptManagerService.PERM_LOCK_ATTEMPTS - 1);

    PinValidateResponse response = new PinValidateResponse(false);

    assertThrows(
        AccountPermanentlyLockedException.class,
        () -> service.processPinAttempt(accountId, response));

    assertTrue(pinSecurity.isPermanentLock());
    assertEquals(AccountStatus.FROZEN, pinSecurity.getAccount().getStatus());
  }

  @Test
  void shouldThrowInternalServiceException_whenSecurityEntityNotFound() {
    when(accountPinSecurityRepository.findByAccount_Id(accountId)).thenReturn(Optional.empty());

    PinValidateResponse response = new PinValidateResponse(true);

    assertThrows(
        CustomInternalServiceException.class, () -> service.processPinAttempt(accountId, response));

    assertThrows(CustomInternalServiceException.class, () -> service.checkPinLock(accountId));
  }
}

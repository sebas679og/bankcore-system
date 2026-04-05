package com.bankcore.accounts.services.complements;

import com.bankcore.accounts.exceptions.AccountPermanentlyLockedException;
import com.bankcore.accounts.exceptions.AccountTemporarilyLockedException;
import com.bankcore.accounts.exceptions.CustomInternalServiceException;
import com.bankcore.accounts.exceptions.IncorrectPinException;
import com.bankcore.accounts.integrations.dto.responses.PinValidateResponse;
import com.bankcore.accounts.models.AccountPinSecurity;
import com.bankcore.accounts.repositories.AccountPinSecurityRepository;
import com.bankcore.accounts.utils.enums.AccountStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for managing PIN validation attempts and enforcing account lockout policies.
 *
 * <p>This service tracks failed PIN attempts for accounts, applies temporary and permanent lockouts
 * based on configurable thresholds, and resets attempts when a PIN is successfully validated. It
 * interacts with the {@link AccountPinSecurityRepository} to persist and retrieve PIN security
 * state.
 *
 * <h2>Lockout Policy:</h2>
 *
 * <ul>
 *   <li>Temporary lock: after {@code 4} failed attempts, the account is locked for {@code 15
 *       minutes}.
 *   <li>Permanent lock: after {@code 8} failed attempts, the account is frozen and permanently
 *       blocked.
 * </ul>
 *
 * <h2>Error Handling:</h2>
 *
 * <ul>
 *   <li>{@link IncorrectPinException} - thrown when a PIN is invalid, including remaining attempts
 *       before lockout.
 *   <li>{@link AccountTemporarilyLockedException} - thrown when the account is temporarily locked.
 *   <li>{@link AccountPermanentlyLockedException} - thrown when the account is permanently locked.
 *   <li>{@link CustomInternalServiceException} - thrown if the PIN security entity cannot be found
 *       (data inconsistency).
 * </ul>
 *
 * @author BankCore Team - Sebastian Orjuela
 * @version 0.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PinAttemptManagerService {

  protected static final int TEMP_LOCK_ATTEMPTS = 4;
  protected static final int PERM_LOCK_ATTEMPTS = 8;
  protected static final Duration TEMP_LOCK_DURATION = Duration.ofMinutes(15);

  private final AccountPinSecurityRepository accountPinSecurityRepository;

  /**
   * Checks if the account is currently locked due to failed PIN attempts.
   *
   * @param accountId the {@link UUID} of the account to check
   * @throws AccountTemporarilyLockedException if the account is temporarily locked
   * @throws AccountPermanentlyLockedException if the account is permanently locked
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  public void checkPinLock(UUID accountId) {
    AccountPinSecurity pinSecurity = getAccountPinSecurityEntity(accountId);
    checkAndThrowIfLocked(pinSecurity);
  }

  /**
   * Processes a PIN validation attempt and updates the account's PIN security state.
   *
   * <p>If the PIN is valid, failed attempts are reset. If invalid, failed attempts are incremented,
   * lockout policies are applied, and an exception is thrown with the number of remaining attempts.
   *
   * @param accountId the {@link UUID} of the account
   * @param response the {@link PinValidateResponse} result of the PIN validation
   * @throws IncorrectPinException if the PIN is invalid
   * @throws AccountTemporarilyLockedException if the account is temporarily locked
   * @throws AccountPermanentlyLockedException if the account is permanently locked
   * @throws CustomInternalServiceException if the PIN security entity is missing
   */
  @Transactional(
      propagation = Propagation.REQUIRES_NEW,
      noRollbackFor = {
        IncorrectPinException.class,
        AccountTemporarilyLockedException.class,
        AccountPermanentlyLockedException.class
      })
  public void processPinAttempt(UUID accountId, PinValidateResponse response) {
    AccountPinSecurity pinSecurity = getAccountPinSecurityEntity(accountId);

    if (response.valid()) {
      if (!isPinAlreadyClean(pinSecurity)) {
        resetAttempts(pinSecurity);
      }
      return;
    }

    registerFailedAttempt(pinSecurity);
    checkAndThrowIfLocked(pinSecurity);

    int remaining = calculateRemainingAttempts(pinSecurity);
    throw new IncorrectPinException(remaining);
  }

  // --- Private helper methods documented inline for clarity ---

  /**
   * Retrieves the {@link AccountPinSecurity} entity for the given account.
   *
   * @param accountId the account ID
   * @return the {@link AccountPinSecurity} entity
   * @throws CustomInternalServiceException if no entity is found
   */
  private AccountPinSecurity getAccountPinSecurityEntity(UUID accountId) {
    return accountPinSecurityRepository
        .findByAccount_Id(accountId)
        .orElseThrow(
            () -> {
              log.error(
                  "Data inconsistency: AccountPinSecurity not found for accountId={}", accountId);
              return new CustomInternalServiceException("Security entity validation error");
            });
  }

  /** Checks if the PIN security state is already clean (no failed attempts, no locks). */
  private boolean isPinAlreadyClean(AccountPinSecurity pinSecurity) {
    return pinSecurity.getFailedAttempts() == 0
        && pinSecurity.getTemporaryLockUntil() == null
        && !pinSecurity.isPermanentLock();
  }

  /** Resets all PIN attempt counters and lock states. */
  private void resetAttempts(AccountPinSecurity pinSecurity) {
    pinSecurity.setFailedAttempts(0);
    pinSecurity.setTemporaryLockUntil(null);
    pinSecurity.setPermanentLock(false);
    pinSecurity.setLastFailedAttemptAt(null);
  }

  /** Registers a failed PIN attempt and applies lockout policies if thresholds are reached. */
  private void registerFailedAttempt(AccountPinSecurity pinSecurity) {
    int attempts = pinSecurity.getFailedAttempts() + 1;
    pinSecurity.setFailedAttempts(attempts);
    pinSecurity.setLastFailedAttemptAt(Instant.now());

    if (attempts >= PERM_LOCK_ATTEMPTS) {
      pinSecurity.getAccount().setStatus(AccountStatus.FROZEN);
      pinSecurity.setPermanentLock(true);
      log.warn(
          "Account permanently blocked: accountId={}, status={}",
          pinSecurity.getAccount().getId(),
          pinSecurity.getAccount().getStatus().name());
    } else if (attempts == TEMP_LOCK_ATTEMPTS) {
      pinSecurity.setTemporaryLockUntil(Instant.now().plus(TEMP_LOCK_DURATION));
      log.warn(
          "Account temporarily locked until {}: accountId={}",
          pinSecurity.getTemporaryLockUntil(),
          pinSecurity.getAccount().getId());
    }
  }

  /** Calculates the number of remaining attempts before lockout. */
  private int calculateRemainingAttempts(AccountPinSecurity pinSecurity) {
    if (pinSecurity.getFailedAttempts() < TEMP_LOCK_ATTEMPTS) {
      return TEMP_LOCK_ATTEMPTS - pinSecurity.getFailedAttempts();
    } else {
      return PERM_LOCK_ATTEMPTS - pinSecurity.getFailedAttempts();
    }
  }

  /** Checks if the account is locked and throws the appropriate exception. */
  private void checkAndThrowIfLocked(AccountPinSecurity pinSecurity) {
    if (pinSecurity.isPermanentLock()) {
      throw new AccountPermanentlyLockedException();
    }

    if (pinSecurity.getTemporaryLockUntil() != null
        && pinSecurity.getTemporaryLockUntil().isAfter(Instant.now())) {
      throw new AccountTemporarilyLockedException(pinSecurity.getTemporaryLockUntil());
    }
  }
}

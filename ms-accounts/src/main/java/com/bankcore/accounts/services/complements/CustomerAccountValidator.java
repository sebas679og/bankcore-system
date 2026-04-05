package com.bankcore.accounts.services.complements;

import com.bankcore.accounts.exceptions.AccountInactiveException;
import com.bankcore.accounts.exceptions.AccountNotFoundException;
import com.bankcore.accounts.integrations.dto.request.PinValidateRequest;
import com.bankcore.accounts.integrations.dto.responses.PinValidateResponse;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.repositories.AccountRepository;
import com.bankcore.accounts.utils.enums.AccountStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Handles validation of customer and account state, including PIN verification.
 *
 * @author BankCore
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
@Service
@RequiredArgsConstructor
public class CustomerAccountValidator {

  private final CustomerValidationService validationService;
  private final PinAttemptManagerService pinSecurityService;
  private final AccountRepository accountRepository;

  /**
   * Validates that the customer is active, the account exists and is active, and the provided PIN
   * is correct.
   *
   * @param customerId the customer ID
   * @param accountId the account ID
   * @param pin the customer's PIN
   * @return the validated {@link AccountEntity}
   */
  public AccountEntity validateCustomerAccountAndPin(UUID customerId, UUID accountId, String pin) {
    validationService.validateCustomerIsActive(customerId);

    AccountEntity account =
        accountRepository
            .findByIdAndCustomerId(accountId, customerId)
            .orElseThrow(AccountNotFoundException::new);

    if (account.getStatus() != AccountStatus.ACTIVE) {
      throw new AccountInactiveException(account.getStatus());
    }

    pinSecurityService.checkPinLock(accountId);

    PinValidateRequest pinRequest = PinValidateRequest.builder().pin(pin).build();
    PinValidateResponse pinResponse = validationService.validateCustomerPin(customerId, pinRequest);

    pinSecurityService.processPinAttempt(accountId, pinResponse);

    return account;
  }
}

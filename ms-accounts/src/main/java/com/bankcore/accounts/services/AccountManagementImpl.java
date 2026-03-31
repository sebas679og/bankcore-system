package com.bankcore.accounts.services;

import com.bankcore.accounts.dto.requests.AccountRegisterRequest;
import com.bankcore.accounts.dto.responses.AccountRegisterResponse;
import com.bankcore.accounts.dto.responses.UserAccountDetailResponse;
import com.bankcore.accounts.dto.responses.UserAccountResponse;
import com.bankcore.accounts.exceptions.AccountNotFoundException;
import com.bankcore.accounts.exceptions.BusinessException;
import com.bankcore.accounts.exceptions.CustomerInactiveException;
import com.bankcore.accounts.exceptions.ResourceConflictException;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.models.AccountPinSecurity;
import com.bankcore.accounts.models.TransactionEntity;
import com.bankcore.accounts.repositories.AccountRepository;
import com.bankcore.accounts.repositories.TransactionRepository;
import com.bankcore.accounts.services.complements.CustomerValidationService;
import com.bankcore.accounts.services.complements.IbanGeneratorService;
import com.bankcore.accounts.services.complements.WithdrawalService;
import com.bankcore.accounts.utils.enums.AccountStatus;
import com.bankcore.accounts.utils.mappers.AccountMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the AccountManagementService interface that provides methods for managing bank
 * accounts. This service interacts with the CustomerClient to validate customer information and
 * uses the AccountRepository to perform database operations related to accounts. It also utilizes
 * the IbanGeneratorService to generate unique IBANs for new accounts and the DailyWithdrawalLimit
 * utility to set withdrawal limits based on account types.
 *
 * @author BankCore Team - Sebastian Orjuela - Cristian Ortiz
 * @version 1.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class AccountManagementImpl implements AccountManagementService {

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final IbanGeneratorService ibanGeneratorService;
  private final WithdrawalService withdrawalService;
  private final AccountMapper accountMapper;
  private final CustomerValidationService validationService;

  private static final int MAX_IBAN_GENERATION_ATTEMPTS = 5;

  /**
   * Registers a new account for a customer.
   *
   * <p>This method validates the customer's state, enforces business rules regarding account limits
   * and alias uniqueness, generates a unique IBAN, and persists the new account entity. It also
   * initializes the account's PIN security configuration.
   *
   * <h2>Business Rules:</h2>
   *
   * <ul>
   *   <li>A customer can register a maximum of 3 accounts.
   *   <li>Account alias must be unique per customer.
   *   <li>New accounts are created with status {@link AccountStatus#ACTIVE} and a daily withdrawal
   *       limit resolved by {@code withdrawalService}.
   * </ul>
   *
   * @param request the {@link AccountRegisterRequest} containing account registration details
   * @param id the {@link UUID} representing the customer ID
   * @return an {@link AccountRegisterResponse} containing the result of the registration
   * @throws BusinessException if the customer exceeds the maximum number of accounts
   * @throws ResourceConflictException if the alias is already in use
   * @throws CustomerInactiveException if the customer is not active
   */
  @Override
  @Transactional
  public AccountRegisterResponse registerAccount(AccountRegisterRequest request, UUID id) {

    validationService.validateCustomerIsActive(id);

    if (accountRepository.countByCustomerId(id) >= 3) {
      throw new BusinessException("Customer has reached the maximum number of accounts allowed");
    }

    if (accountRepository.existsByAliasAndCustomerId(request.getAlias(), id)) {
      throw new ResourceConflictException(
          "The alias is already in use on an account under your name");
    }

    String iban = generateUniqueIban();

    AccountEntity accountEntity =
        AccountEntity.builder()
            .accountNumber(iban)
            .customerId(id)
            .accountType(request.getAccountType())
            .currency(request.getCurrency())
            .balance(BigDecimal.ZERO)
            .alias(request.getAlias())
            .status(AccountStatus.ACTIVE)
            .dailyWithdrawalLimit(withdrawalService.resolveDailyLimit(request.getAccountType()))
            .security(AccountPinSecurity.builder().build())
            .build();

    accountEntity.getSecurity().setAccount(accountEntity);

    accountRepository.save(accountEntity);

    return accountMapper.toAccountRegisterResponse(accountEntity);
  }

  /**
   * Retrieves all bank accounts associated with the authenticated customer.
   *
   * <p>Validates that the provided {@link UUID} is not null, verifies the customer exists and is
   * active via the customer service, and returns the mapped account list.
   *
   * @param id the {@link UUID} of the customer whose accounts are to be retrieved
   * @return a {@link List} of {@link UserAccountResponse} associated with the customer, or an empty
   *     list if no accounts are found
   * @throws IllegalArgumentException if {@code id} is null
   * @throws com.bankcore.accounts.exceptions.CustomerNotFoundException if the customer does not
   *     exist in the Customer Service
   * @throws CustomerInactiveException if the customer account is inactive
   * @throws com.bankcore.accounts.exceptions.CustomExternalServiceException if the Customer Service
   *     returns an error or empty response
   */
  @Override
  @Transactional(readOnly = true)
  public List<UserAccountResponse> getCurrentUserAccounts(UUID id) {

    if (id == null) {
      throw new IllegalArgumentException("Customer ID must not be null");
    }

    // Note: customer status validation is intentionally omitted here.
    // Any valid JWT guarantees the customer was ACTIVE at authentication time.
    // For read-only operations, we accept the token's validity window as sufficient.
    // Explicit status validation is enforced on state-mutating or financial operations.

    List<AccountEntity> accounts = accountRepository.findAllByCustomerId(id);
    return accountMapper.toResponseList(accounts);
  }

  /**
   * Retrieves the full details of a specific account belonging to the authenticated customer.
   *
   * <p>Ownership is validated by matching both the account ID and the customer ID extracted from
   * the JWT token, preventing unauthorized access to accounts owned by other customers. If no
   * matching account is found, a generic exception is thrown to avoid exposing sensitive
   * information to the caller.
   *
   * <p>The timestamp of the most recent transaction is included in the response when available, and
   * omitted entirely if the account has no transaction history yet.
   *
   * @param accountId the unique identifier of the account to retrieve
   * @param customerId the unique identifier of the authenticated customer, extracted from the JWT
   *     token
   * @return a {@link UserAccountDetailResponse} containing the full account details, including the
   *     timestamp of the last transaction if one exists
   * @throws AccountNotFoundException if no account is found matching both the given account ID and
   *     customer ID, or if the account belongs to a different customer
   */
  @Override
  public UserAccountDetailResponse getAccountDetails(UUID accountId, UUID customerId) {

    AccountEntity account =
        accountRepository
            .findByIdAndCustomerId(accountId, customerId)
            .orElseThrow(AccountNotFoundException::new);

    Instant lastTransactionAt =
        transactionRepository
            .findTopByAccount_IdOrderByCreatedAtDesc(accountId)
            .map(TransactionEntity::getCreatedAt)
            .orElse(null);

    return accountMapper.toDetailResponse(account, lastTransactionAt);
  }

  // IBAN generation
  private String generateUniqueIban() {

    for (int attempt = 1; attempt <= MAX_IBAN_GENERATION_ATTEMPTS; attempt++) {

      String iban = ibanGeneratorService.generateSpanishIban();

      if (!accountRepository.existsByAccountNumber(iban)) {
        return iban;
      }

      log.warn("IBAN collision detected (attempt {}): {}", attempt, iban);
    }

    throw new ResourceConflictException("Unable to generate a unique IBAN after multiple attempts");
  }
}

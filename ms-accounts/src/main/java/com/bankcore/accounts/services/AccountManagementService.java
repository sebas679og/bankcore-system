package com.bankcore.accounts.services;

import com.bankcore.accounts.dto.requests.AccountRegisterRequest;
import com.bankcore.accounts.dto.responses.AccountRegisterResponse;
import com.bankcore.accounts.dto.responses.UserAccountDetailResponse;
import com.bankcore.accounts.dto.responses.UserAccountResponse;
import java.util.List;
import java.util.UUID;

/**
 * Interface for managing bank accounts, providing methods for account registration and other
 * account-related operations.
 *
 * @author BankCore Team - Sebastian Orjuela - Cristian Ortiz
 * @version 0.1.0
 */
public interface AccountManagementService {

  /**
   * Registers a new account for the given customer.
   *
   * @param request the {@link AccountRegisterRequest} containing account registration details
   * @param id the {@link UUID} representing the customer ID
   * @return an {@link AccountRegisterResponse} containing the result of the registration
   */
  AccountRegisterResponse registerAccount(AccountRegisterRequest request, UUID id);

  /**
   * Retrieves all accounts associated with the given customer.
   *
   * @param id the {@link UUID} representing the customer ID
   * @return a list of {@link UserAccountResponse} objects representing the customer's accounts
   */
  List<UserAccountResponse> getCurrentUserAccounts(UUID id);

  /**
   * Retrieves detailed information for a specific account belonging to the given customer.
   *
   * @param accountId the {@link UUID} representing the account ID
   * @param id the {@link UUID} representing the customer ID
   * @return a {@link UserAccountDetailResponse} containing detailed account information
   */
  UserAccountDetailResponse getAccountDetails(UUID accountId, UUID id);
}

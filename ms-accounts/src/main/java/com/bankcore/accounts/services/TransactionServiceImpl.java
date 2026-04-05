package com.bankcore.accounts.services;

import com.bankcore.accounts.dto.requests.TransactionQueryParams;
import com.bankcore.accounts.dto.requests.TransactionRequest;
import com.bankcore.accounts.dto.requests.TransferRequest;
import com.bankcore.accounts.dto.responses.TransactionResponse;
import com.bankcore.accounts.dto.responses.TransactionsHistoryResponse;
import com.bankcore.accounts.dto.responses.TransferResponse;
import com.bankcore.accounts.exceptions.AccountInactiveException;
import com.bankcore.accounts.exceptions.AccountNotFoundException;
import com.bankcore.accounts.exceptions.BusinessException;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.services.complements.CustomerAccountValidator;
import com.bankcore.accounts.services.processors.DepositProcessor;
import com.bankcore.accounts.services.processors.TransactionHistoryProcessor;
import com.bankcore.accounts.services.processors.TransferProcessor;
import com.bankcore.accounts.services.processors.WithdrawalProcessor;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coordinator service that manages deposit and transfer operations for customer accounts.
 *
 * <p>This service delegates the following responsibilities to specialized components:
 *
 * <ul>
 *   <li>{@link CustomerAccountValidator}: validates customer and account status, including PIN
 *       verification.
 *   <li>{@link DepositProcessor}: handles deposit processing and persistence.
 *   <li>{@link TransferProcessor}: handles transfer processing, including destination resolution,
 *       balance updates, and transaction persistence.
 * </ul>
 *
 * <p>By delegating responsibilities, this class maintains a clear orchestration role, following the
 * Single Responsibility Principle (SRP).
 *
 * @author BankCore
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

  private final CustomerAccountValidator validator;
  private final DepositProcessor depositProcessor;
  private final WithdrawalProcessor withdrawalProcessor;
  private final TransferProcessor transferProcessor;
  private final TransactionHistoryProcessor transactionHistory;

  /**
   * Executes a deposit transaction for a customer's account.
   *
   * <p>The method performs the following steps:
   *
   * <ol>
   *   <li>Validates the customer and account state, including PIN verification using {@link
   *       CustomerAccountValidator}.
   *   <li>Delegates deposit processing to {@link DepositProcessor}, which updates the account
   *       balance and saves the transaction.
   * </ol>
   *
   * @param request the {@link TransactionRequest} containing deposit details
   * @param accountId the {@link UUID} of the account to deposit into
   * @param customerId the {@link UUID} of the customer performing the transaction
   * @return a {@link TransactionResponse} containing the result of the deposit
   */
  @Override
  public TransactionResponse makeDeposit(
      TransactionRequest request, UUID accountId, UUID customerId) {
    AccountEntity account =
        validator.validateCustomerAccountAndPin(customerId, accountId, request.getPin());
    return depositProcessor.processDeposit(account, request.getAmount(), request.getDescription());
  }

  /**
   * Executes a withdrawal transaction after validating customer, account, PIN, balance, and limits.
   *
   * <p>The method performs the following steps:
   *
   * <ol>
   *   <li>Validates that the customer exists and is active.
   *   <li>Retrieves the account and ensures it belongs to the customer.
   *   <li>Checks that the account is active.
   *   <li>Validates the ATM PIN.
   *   <li>Verifies if the account has sufficient balance.
   *   <li>Verifies if the withdrawal exceeds the daily limit (calculated in UTC).
   *   <li>Reduces the account balance and persists the WITHDRAWAL transaction.
   * </ol>
   *
   * @param request the {@link TransactionRequest} containing withdrawal details
   * @param accountId the {@link UUID} of the account to withdraw from
   * @param customerId the {@link UUID} of the customer performing the transaction
   * @return a {@link TransactionResponse} containing the result of the withdrawal
   * @throws AccountNotFoundException if the account does not exist
   * @throws AccountInactiveException if the account is inactive
   * @throws BusinessException if funds are insufficient or daily limit is exceeded
   */
  @Override
  @Transactional
  public TransactionResponse makeWithdrawal(
      TransactionRequest request, UUID accountId, UUID customerId) {

    AccountEntity account =
        validator.validateCustomerAccountAndPin(customerId, accountId, request.getPin());

    if (account.getBalance().compareTo(request.getAmount()) < 0) {
      throw new BusinessException("INSUFFICIENT_FUNDS");
    }

    return withdrawalProcessor.processWithdrawal(
        account, request.getAmount(), request.getDescription());
  }

  /**
   * Executes a transfer from a source account to a destination account.
   *
   * <p>The method performs the following steps:
   *
   * <ol>
   *   <li>Validates the source account and customer state, including PIN verification using {@link
   *       CustomerAccountValidator}.
   *   <li>Delegates transfer processing to {@link TransferProcessor}, which resolves the
   *       destination account, calculates balances, and persists transfer and transaction records.
   * </ol>
   *
   * @param request the {@link TransferRequest} containing transfer details
   * @param customerId the {@link UUID} of the customer initiating the transfer
   * @return a {@link TransferResponse} containing the result of the transfer
   */
  @Override
  public TransferResponse makeTransfer(TransferRequest request, UUID customerId) {
    AccountEntity sourceAccount =
        validator.validateCustomerAccountAndPin(
            customerId, request.getSourceAccountId(), request.getPin());
    return transferProcessor.processTransfer(sourceAccount, request);
  }

  /**
   * Retrieves the transaction history for a given account, applying optional filters such as type,
   * date range, and pagination.
   *
   * <p>This method delegates the actual processing to {@link
   * TransactionHistoryProcessor#getTransactions(UUID, UUID, TransactionQueryParams)}, ensuring that
   * the service layer handles validation, filtering, and mapping.
   *
   * @param accountId the unique identifier of the account
   * @param customerId the unique identifier of the customer
   * @param filters the query parameters including pagination, type, and date filters
   * @return a {@link TransactionsHistoryResponse} containing transaction history and pagination
   *     metadata
   * @see TransactionHistoryProcessor
   * @see TransactionsHistoryResponse
   * @see TransactionQueryParams
   */
  @Override
  public TransactionsHistoryResponse getTransactionsHistory(
      UUID accountId, UUID customerId, TransactionQueryParams filters) {
    return transactionHistory.getTransactions(accountId, customerId, filters);
  }
}

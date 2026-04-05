package com.bankcore.accounts.services.processors;

import com.bankcore.accounts.dto.requests.TransferRequest;
import com.bankcore.accounts.dto.responses.TransferResponse;
import com.bankcore.accounts.exceptions.AccountInactiveException;
import com.bankcore.accounts.exceptions.AccountNotFoundException;
import com.bankcore.accounts.exceptions.InsufficientBalanceException;
import com.bankcore.accounts.exceptions.InvalidTransferDestinationException;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.models.TransactionEntity;
import com.bankcore.accounts.models.TransferEntity;
import com.bankcore.accounts.repositories.AccountRepository;
import com.bankcore.accounts.repositories.TransactionRepository;
import com.bankcore.accounts.repositories.TransferRepository;
import com.bankcore.accounts.services.complements.CustomerValidationService;
import com.bankcore.accounts.utils.enums.AccountStatus;
import com.bankcore.accounts.utils.enums.TransactionStatus;
import com.bankcore.accounts.utils.enums.TransactionType;
import com.bankcore.accounts.utils.enums.TransferStatus;
import com.bankcore.accounts.utils.mappers.TransactionMapper;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Handles transfer processing between accounts.
 *
 * @author BankCore
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
@Service
@RequiredArgsConstructor
public class TransferProcessor {

  private final AccountRepository accountRepository;
  private final TransferRepository transferRepository;
  private final TransactionRepository transactionRepository;
  private final CustomerValidationService validationService;
  private final TransactionMapper transactionMapper;

  /**
   * Processes a transfer from source account to a destination account.
   *
   * @param sourceAccount the source account
   * @param request the {@link TransferRequest} details
   * @return {@link TransferResponse} with the result
   */
  @Transactional
  public TransferResponse processTransfer(AccountEntity sourceAccount, TransferRequest request) {

    if (request.getDestinationAccountNumber().equals(sourceAccount.getAccountNumber())) {
      throw new InvalidTransferDestinationException("Transfer to the same account is not allowed");
    }

    // Resolve destination account
    AccountEntity destinationAccount =
        resolveDestinationAccount(request.getDestinationAccountNumber());
    String beneficiaryName = resolveRecipientName(destinationAccount);

    // Calculate total debited (amount + fees)
    BigDecimal fee = BigDecimal.ZERO;
    BigDecimal totalDebited = request.getAmount().add(fee);
    if (sourceAccount.getBalance().compareTo(totalDebited) < 0) {
      throw new InsufficientBalanceException();
    }

    BigDecimal sourceBalanceAfter = sourceAccount.getBalance().subtract(totalDebited);

    // Save transfer record
    TransferEntity transfer =
        TransferEntity.builder()
            .account(sourceAccount)
            .destinationAccountNumber(request.getDestinationAccountNumber())
            .amount(request.getAmount())
            .fee(fee)
            .description(request.getDescription())
            .status(destinationAccount != null ? TransferStatus.COMPLETED : TransferStatus.PENDING)
            .build();

    transferRepository.save(transfer);

    // Process source transaction
    processTransaction(
        sourceAccount,
        request.getAmount(),
        sourceBalanceAfter,
        request.getDestinationAccountNumber(),
        beneficiaryName,
        request.getDescription(),
        TransactionType.TRANSFER_OUT);
    sourceAccount.setBalance(sourceBalanceAfter);
    accountRepository.save(sourceAccount);

    // Process destination transaction if account exists
    if (destinationAccount != null) {
      BigDecimal destBalanceAfter = destinationAccount.getBalance().add(request.getAmount());
      String senderName = validationService.getFullNameCustomerById(sourceAccount.getCustomerId());

      processTransaction(
          destinationAccount,
          request.getAmount(),
          destBalanceAfter,
          sourceAccount.getAccountNumber(),
          senderName,
          request.getDescription(),
          TransactionType.TRANSFER_IN);

      destinationAccount.setBalance(destBalanceAfter);
      accountRepository.save(destinationAccount);
    }

    return transactionMapper.toTransferResponse(
        transfer, sourceAccount.getAccountNumber(), beneficiaryName, totalDebited);
  }

  private AccountEntity resolveDestinationAccount(String destinationAccountNumber) {
    if (!accountRepository.existsByAccountNumber(destinationAccountNumber)) {
      return null;
    }

    AccountEntity destination =
        accountRepository
            .findByAccountNumber(destinationAccountNumber)
            .orElseThrow(() -> new AccountNotFoundException("Destination account not found"));

    if (destination.getStatus() != AccountStatus.ACTIVE) {
      throw new AccountInactiveException(
          destination.getStatus(), "Destination account is not active.");
    }

    return destination;
  }

  private String resolveRecipientName(AccountEntity destinationAccount) {
    if (destinationAccount == null) {
      return null;
    }
    return validationService.getFullNameCustomerById(destinationAccount.getCustomerId());
  }

  private void processTransaction(
      AccountEntity account,
      BigDecimal amount,
      BigDecimal balanceAfter,
      String counterpartyAccountNumber,
      String counterpartyName,
      String description,
      TransactionType type) {
    TransactionEntity transaction =
        TransactionEntity.builder()
            .account(account)
            .type(type)
            .amount(amount)
            .balanceAfter(balanceAfter)
            .counterpartyAccountNumber(counterpartyAccountNumber)
            .counterpartyName(counterpartyName)
            .description(description)
            .status(TransactionStatus.COMPLETED)
            .build();
    transactionRepository.save(transaction);
  }
}

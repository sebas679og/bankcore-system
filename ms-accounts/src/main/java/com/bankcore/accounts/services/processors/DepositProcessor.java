package com.bankcore.accounts.services.processors;

import com.bankcore.accounts.dto.responses.TransactionResponse;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.models.TransactionEntity;
import com.bankcore.accounts.repositories.AccountRepository;
import com.bankcore.accounts.repositories.TransactionRepository;
import com.bankcore.accounts.utils.enums.TransactionStatus;
import com.bankcore.accounts.utils.enums.TransactionType;
import com.bankcore.accounts.utils.mappers.TransactionMapper;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Handles deposit processing and persistence.
 *
 * @author BankCore
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
@Service
@RequiredArgsConstructor
public class DepositProcessor {

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final TransactionMapper transactionMapper;

  /**
   * Processes a deposit for the given account.
   *
   * @param account the account to deposit into
   * @param amount the deposit amount
   * @param description a description for the transaction
   * @return a {@link TransactionResponse} with the result
   */
  @Transactional
  public TransactionResponse processDeposit(
      AccountEntity account, BigDecimal amount, String description) {
    BigDecimal balanceBefore = account.getBalance();
    BigDecimal newBalance = balanceBefore.add(amount);

    TransactionEntity transaction =
        TransactionEntity.builder()
            .account(account)
            .type(TransactionType.DEPOSIT)
            .amount(amount)
            .balanceAfter(newBalance)
            .description(description)
            .status(TransactionStatus.COMPLETED)
            .build();

    transactionRepository.save(transaction);

    account.setBalance(newBalance);
    accountRepository.save(account);

    return transactionMapper.toTransactionResponse(transaction, balanceBefore);
  }
}

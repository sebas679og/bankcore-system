package com.bankcore.accounts.services.processors;

import com.bankcore.accounts.dto.responses.TransactionResponse;
import com.bankcore.accounts.exceptions.BusinessException;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.models.TransactionEntity;
import com.bankcore.accounts.repositories.AccountRepository;
import com.bankcore.accounts.repositories.TransactionRepository;
import com.bankcore.accounts.utils.enums.TransactionStatus;
import com.bankcore.accounts.utils.enums.TransactionType;
import com.bankcore.accounts.utils.mappers.TransactionMapper;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Specialized component responsible for processing withdrawal transactions.
 *
 * <p>This class handles:
 *
 * <ul>
 *   <li>Calculating daily withdrawal totals to enforce limits.
 *   <li>Verifying that the transaction does not exceed the daily withdrawal limit.
 *   <li>Updating account balances.
 *   <li>Persisting the transaction and the updated account state.
 * </ul>
 *
 * @author BankCore
 * @author Cristian Ortiz
 * @version 0.1.0
 */
@Service
@RequiredArgsConstructor
public class WithdrawalProcessor {

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final TransactionMapper transactionMapper;

  /**
   * Processes a withdrawal transaction.
   *
   * <p>Performs a daily limit check based on the current UTC day, updates the account balance, and
   * persists a record of the transaction.
   *
   * @param account the {@link AccountEntity} from which funds are being withdrawn
   * @param amount the {@link BigDecimal} amount to withdraw
   * @param description an optional description for the transaction
   * @return a {@link TransactionResponse} containing details of the completed withdrawal
   * @throws BusinessException if the daily withdrawal limit is exceeded
   */
  @Transactional
  public TransactionResponse processWithdrawal(
      AccountEntity account, BigDecimal amount, String description) {

    // Daily Limit Check (UTC Calendar Day)
    Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();

    BigDecimal dailyTotal =
        transactionRepository.calculateDailyWithdrawalTotal(
            account.getId(), TransactionType.WITHDRAWAL, TransactionStatus.COMPLETED, startOfDay);

    if (dailyTotal.add(amount).compareTo(account.getDailyWithdrawalLimit()) > 0) {
      throw new BusinessException("DAILY_LIMIT_EXCEEDED");
    }

    BigDecimal balanceBefore = account.getBalance();
    BigDecimal newBalance = balanceBefore.subtract(amount);

    TransactionEntity transaction =
        TransactionEntity.builder()
            .account(account)
            .type(TransactionType.WITHDRAWAL)
            .amount(amount)
            .balanceAfter(newBalance)
            .description(description)
            .status(TransactionStatus.COMPLETED)
            .build();

    account.setBalance(newBalance);

    transactionRepository.save(transaction);
    accountRepository.save(account);

    return transactionMapper.toTransactionResponse(transaction, balanceBefore);
  }
}

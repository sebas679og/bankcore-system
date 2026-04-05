package com.bankcore.accounts;

import com.bankcore.accounts.dto.requests.TransactionRequest;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.models.TransactionEntity;
import com.bankcore.accounts.services.complements.IbanGeneratorService;
import com.bankcore.accounts.utils.enums.TransactionStatus;
import com.bankcore.accounts.utils.enums.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class that provides mock {@link TransactionEntity} and {@link TransactionRequest}
 * instances for testing purposes.
 *
 * <p>This class centralizes the creation of sample transactions with configurable attributes such
 * as account, type, status, and creation time. It ensures consistency across test scenarios by
 * abstracting transaction construction and reducing boilerplate code.
 *
 * <p>Typical usage includes simulating deposits, transfers, or pending transactions with randomized
 * attributes. It also supports generating lists of transactions with incremental timestamps to
 * mimic realistic activity.
 *
 * @author BankcoreTeam
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
public class TransactionDataProvider {

  private static final IbanGeneratorService generatorService = new IbanGeneratorService();

  private static final Random RANDOM = new Random();

  public static final String INVALID_UUID = "e7c6be34-c77b-4afa-aebb-327354a9fe0z";

  /**
   * Creates a mock {@link TransactionEntity} with default attributes.
   *
   * <p>The transaction simulates a deposit of 100 EUR, updates the balance, and sets a completed
   * status with a mock counterparty.
   *
   * @param account the account associated with the transaction
   * @return a mock {@link TransactionEntity} representing a deposit
   */
  public static TransactionEntity createMockTransaction(AccountEntity account) {
    return TransactionEntity.builder()
        .account(account)
        .type(TransactionType.DEPOSIT)
        .amount(new BigDecimal("100.00"))
        .balanceAfter(new BigDecimal("1100.00"))
        .description("Mock transaction")
        .counterpartyAccountNumber("MOCK9121000418450200051332")
        .counterpartyName("Mock Counterparty")
        .status(TransactionStatus.COMPLETED)
        .createdAt(Instant.now())
        .build();
  }

  /**
   * Creates a mock {@link TransactionEntity} with a custom creation time.
   *
   * <p>Useful for simulating transactions at specific points in time while keeping other attributes
   * fixed (deposit type, amount, status).
   *
   * @param account the account associated with the transaction
   * @param createdAt the timestamp to assign to the transaction
   * @return a mock {@link TransactionEntity} with the given creation time
   */
  public static TransactionEntity createMockTransaction(AccountEntity account, Instant createdAt) {
    return TransactionEntity.builder()
        .account(account)
        .type(TransactionType.DEPOSIT)
        .amount(new BigDecimal("100.00"))
        .balanceAfter(new BigDecimal("1100.00"))
        .description("Mock transaction")
        .counterpartyAccountNumber("MOCK9121000418450200051332")
        .counterpartyName("Mock Counterparty")
        .status(TransactionStatus.COMPLETED)
        .createdAt(createdAt)
        .build();
  }

  /**
   * Creates a mock {@link TransactionRequest} with fixed attributes.
   *
   * <p>The request simulates a deposit of 100 EUR with a test description and a provided PIN for
   * authorization.
   *
   * @param pin the PIN used for transaction authorization
   * @return a mock {@link TransactionRequest} initialized with the given PIN
   */
  public static TransactionRequest createMockTransactionRequest(String pin) {
    return TransactionRequest.builder()
        .amount(BigDecimal.valueOf(100.00))
        .description("test-description")
        .pin(pin)
        .build();
  }

  /**
   * Creates a list of mock {@link TransactionEntity} instances with randomized attributes.
   *
   * <p>Transactions are generated with varying types (deposit, transfer, etc.), optional
   * counterparty details, and statuses based on counterparty presence. Each transaction is
   * timestamped with a 5-minute increment from the base time.
   *
   * @param registers the number of transactions to generate
   * @param account the account associated with the transactions
   * @param baseTime the starting timestamp for the first transaction
   * @return a list of mock {@link TransactionEntity} objects
   */
  public static List<TransactionEntity> createMockTransactions(
      Integer registers, AccountEntity account, Instant baseTime) {
    List<TransactionEntity> transactions = new ArrayList<>();

    TransactionType[] types = TransactionType.values();

    for (int i = 0; i < registers; i++) {
      // Choose a random transaction type
      TransactionType type = types[RANDOM.nextInt(types.length)];

      // Initialize counterparty fields
      String counterpartyAccountNumber = null;
      String counterpartyName = null;

      // If it is a transfer, generate IBAN and optional name
      if (type == TransactionType.TRANSFER_IN || type == TransactionType.TRANSFER_OUT) {
        counterpartyAccountNumber = generatorService.generateSpanishIban();
        // 50% probability of having a name
        counterpartyName = RANDOM.nextBoolean() ? "Counterparty " + (i + 1) : null;
      }

      // Define status based on counterpartyName
      TransactionStatus status =
          (counterpartyName != null) ? TransactionStatus.COMPLETED : TransactionStatus.PENDING;

      // Calculate date with 5-minute increments
      Instant createdAt = baseTime.truncatedTo(ChronoUnit.MILLIS).plus(i * 5L, ChronoUnit.MINUTES);

      // Build transaction
      TransactionEntity tx =
          TransactionEntity.builder()
              .account(account)
              .type(type)
              .amount(new BigDecimal("100.00")) // you can randomize if needed
              .balanceAfter(new BigDecimal("1100.00")) // same here
              .description("Mock transaction " + (i + 1))
              .counterpartyAccountNumber(counterpartyAccountNumber)
              .counterpartyName(counterpartyName)
              .status(status)
              .createdAt(createdAt)
              .build();

      transactions.add(tx);
    }

    return transactions;
  }
}

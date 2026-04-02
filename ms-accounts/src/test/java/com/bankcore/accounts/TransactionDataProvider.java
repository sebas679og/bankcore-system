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

public class TransactionDataProvider {

  private static final IbanGeneratorService generatorService = new IbanGeneratorService();

  private static final Random RANDOM = new Random();

  public static final String INVALID_UUID = "e7c6be34-c77b-4afa-aebb-327354a9fe0z";

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

  public static TransactionRequest createMockTransactionRequest(String pin) {
    return TransactionRequest.builder()
        .amount(BigDecimal.valueOf(100.00))
        .description("test-description")
        .pin(pin)
        .build();
  }

  public static List<TransactionEntity> createMockTransactions(
      Integer registers, AccountEntity account, Instant baseTime) {
    List<TransactionEntity> transactions = new ArrayList<>();

    TransactionType[] types = TransactionType.values();

    for (int i = 0; i < registers; i++) {
      // Escoger tipo de transacción aleatorio
      TransactionType type = types[RANDOM.nextInt(types.length)];

      // Inicializamos counterparty fields
      String counterpartyAccountNumber = null;
      String counterpartyName = null;

      // Si es transferencia, generamos IBAN y nombre opcional
      if (type == TransactionType.TRANSFER_IN || type == TransactionType.TRANSFER_OUT) {
        counterpartyAccountNumber = generatorService.generateSpanishIban();
        // 50% de probabilidad de tener nombre
        counterpartyName = RANDOM.nextBoolean() ? "Counterparty " + (i + 1) : null;
      }

      // Definir status según counterpartyName
      TransactionStatus status =
          (counterpartyName != null) ? TransactionStatus.COMPLETED : TransactionStatus.PENDING;

      // Calcular fecha con incremento de 5 minutos
      Instant createdAt = baseTime.truncatedTo(ChronoUnit.MILLIS).plus(i * 5L, ChronoUnit.MINUTES);

      // Construir transacción
      TransactionEntity tx =
          TransactionEntity.builder()
              .account(account)
              .type(type)
              .amount(new BigDecimal("100.00")) // puedes randomizar si quieres
              .balanceAfter(new BigDecimal("1100.00")) // idem
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

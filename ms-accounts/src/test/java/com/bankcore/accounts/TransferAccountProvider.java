package com.bankcore.accounts;

import com.bankcore.accounts.dto.requests.TransferRequest;
import java.math.BigDecimal;
import java.util.UUID;

public class TransferAccountProvider {

  public static TransferRequest createMockTransferRequest(
      UUID sourceAccount, String pin, String destinationAccount) {
    return TransferRequest.builder()
        .sourceAccountId(sourceAccount)
        .destinationAccountNumber(destinationAccount)
        .amount(BigDecimal.valueOf(10000.00))
        .description("test-transfer")
        .pin(pin)
        .build();
  }
}

package com.bankcore.accounts;

import com.bankcore.accounts.dto.requests.TransferRequest;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Utility class that provides mock {@link TransferRequest} instances for testing.
 *
 * <p>This class centralizes the creation of sample transfer requests with predefined attributes
 * such as amount, description, and PIN. It ensures consistency across test scenarios by abstracting
 * request construction and reducing boilerplate.
 *
 * @author BankcoreTeam
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
public class TransferAccountProvider {

  /**
   * Creates a mock {@link TransferRequest} with fixed attributes.
   *
   * <p>The request simulates a transfer of 10,000 EUR with a test description and a provided PIN.
   * It allows specifying the source account ID and destination account number to adapt to different
   * test cases.
   *
   * @param sourceAccount the UUID of the source account
   * @param pin the PIN used for authorization
   * @param destinationAccount the destination account number
   * @return a mock {@link TransferRequest} initialized with the given parameters
   */
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

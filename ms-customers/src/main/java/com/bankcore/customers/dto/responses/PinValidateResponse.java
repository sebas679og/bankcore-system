package com.bankcore.customers.dto.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Response DTO returned by the PIN validation service.
 *
 * <p>This object represents the result of a PIN validation request performed by internal services
 * when authorizing sensitive operations such as financial transactions between accounts.
 *
 * <p>The {@code valid} field indicates whether the provided PIN matches the one associated with the
 * account.
 *
 * <p>This response is typically consumed by other internal services to determine if a transaction
 * can proceed.
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 0.1.0
 */
@Setter
@Getter
@Builder
public class PinValidateResponse {

  /**
   * Indicates whether the provided PIN is valid.
   *
   * <p>{@code true} if the PIN matches the stored credential, {@code false} otherwise.
   */
  private boolean valid;
}

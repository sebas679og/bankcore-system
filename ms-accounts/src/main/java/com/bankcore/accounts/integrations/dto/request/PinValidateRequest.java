package com.bankcore.accounts.integrations.dto.request;

import lombok.Builder;
import lombok.Data;

/**
 * Request object used to validate a PIN.
 *
 * <p>This Data Transfer Object (DTO) encapsulates the PIN value provided by the client during
 * authentication or security verification processes.
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 0.1.0
 */
@Builder
@Data
public class PinValidateRequest {

  /** The PIN value provided by the client for validation. */
  private String pin;
}

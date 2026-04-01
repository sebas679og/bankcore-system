package com.bankcore.customers.dto.requests;

import com.bankcore.customers.utils.validators.ValidAtmPin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Request object used to validate an ATM PIN input.
 *
 * <p>This class ensures that the provided PIN meets the following criteria:
 *
 * <ul>
 *   <li>Cannot be {@code null} ({@link NotNull})
 *   <li>Must be exactly 4 digits ({@link Size})
 *   <li>Must contain only numeric characters ({@link Pattern})
 *   <li>Must comply with custom validation rules defined by {@link ValidAtmPin}
 * </ul>
 *
 * <p>If the PIN does not meet these constraints, validation errors will be raised during request
 * processing.
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 0.1.0
 * @see ValidAtmPin
 */
@Getter
@Setter
@Builder
public class PinValidateRequest {

  @NotNull(message = "ATM Pin cannot be null")
  @Size(min = 4, max = 4, message = "ATM Pin must be exactly 4 digits")
  @Pattern(regexp = "\\d+", message = "ATM Pin must contain only numbers")
  @ValidAtmPin
  private String pin;
}

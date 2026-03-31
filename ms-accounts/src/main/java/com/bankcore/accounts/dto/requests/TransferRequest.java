package com.bankcore.accounts.dto.requests;

import com.bankcore.accounts.utils.validators.iban.ValidIban;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a request to initiate a transfer operation.
 *
 * <p>This Data Transfer Object (DTO) is used to capture the necessary information for creating a
 * new transfer, including source account, destination IBAN, amount, optional description, and ATM
 * PIN for authentication.
 *
 * <h2>Validation</h2>
 *
 * <ul>
 *   <li>Ensures required fields are present using Bean Validation annotations.
 *   <li>Validates destination IBAN format via {@link ValidIban}.
 *   <li>Enforces minimum amount constraints.
 *   <li>Requires a valid ATM PIN:
 *       <ul>
 *         <li>Must not be null.
 *         <li>Must be exactly 4 digits.
 *         <li>Must contain only numeric characters.
 *       </ul>
 * </ul>
 *
 * @author BankcoreTeam - Sebastian Orjuela
 * @version 1.0
 */
@Data
@Builder
public class TransferRequest {

  @NotNull(message = "Source account ID is required")
  private UUID sourceAccountId;

  @NotNull(message = "The IBAN of the destination account is required")
  @ValidIban(message = "The destination IBAN is not valid")
  private String destinationAccountNumber;

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "1.0", message = "Amount must be at least 1.0")
  private BigDecimal amount;

  private String description;

  @NotNull(message = "PIN cannot be null")
  @NotBlank(message = "PIN cannot be empty")
  @Pattern(regexp = "\\d{4}", message = "PIN must be exactly 4 numeric digits")
  private String pin;
}

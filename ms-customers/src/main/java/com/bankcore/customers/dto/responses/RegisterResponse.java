package com.bankcore.customers.dto.responses;

import com.bankcore.customers.utils.enums.CustomerStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) representing the response returned after a successful user
 * registration.
 *
 * <p>This object contains non-sensitive user information generated during the registration process
 * and returned to the client. Sensitive data such as password or ATM PIN are intentionally
 * excluded.
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 1.0
 */
@Getter
@Setter
@Builder
public class RegisterResponse {

  /** Unique identifier assigned to the registered user. */
  private UUID id;

  /** Government-issued identification number of the user. */
  private String dni;

  /** User's full name, typically composed of first name and last name. */
  private String fullName;

  /** User's registered email address. */
  private String email;

  /** Current status of the customer account (e.g., ACTIVE, INACTIVE, BLOCKED). */
  private CustomerStatus status;

  /**
   * Timestamp indicating when the account was created. Stored as an {@link Instant} to ensure
   * timezone-independent precision.
   */
  private Instant createdDate;
}

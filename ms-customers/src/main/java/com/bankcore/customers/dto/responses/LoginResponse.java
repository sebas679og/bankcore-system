package com.bankcore.customers.dto.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object representing the successful authentication response.
 *
 * <p>Contains the security token required for subsequent API requests and basic metadata regarding
 * the session and the authenticated customer.
 *
 * @author BankCore Team - Cristian Ortiz
 * @version 0.1.0
 */
@Getter
@Setter
@Builder
public class LoginResponse {

  /** The JSON Web Token (JWT) issued to the customer. */
  private String token;

  /** The type of token provided "Bearer". */
  private String tokenType;

  /** The remaining lifetime of the token in seconds. */
  private Long expiresIn;

  /** The unique identifier of the authenticated customer. */
  private String customerId;
}

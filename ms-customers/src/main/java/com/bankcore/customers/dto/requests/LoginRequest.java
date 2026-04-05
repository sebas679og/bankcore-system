package com.bankcore.customers.dto.requests;

import com.bankcore.customers.utils.validators.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for client authentication requests.
 *
 * <p>This class captures the necessary credentials (email and password) required to authenticate a
 * user within the bank's core system.
 *
 * @author BankCore Team
 * @author Cristian Ortiz
 * @version 0.1.0
 */
@Getter
@Setter
@Builder
public class LoginRequest {

  /**
   * The registered email address of the client. Must be a valid email format and cannot be blank.
   */
  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  /**
   * The client's secret password. Must comply with the security policy defined in {@link
   * ValidPassword}.
   */
  @NotBlank(message = "Password is required")
  @ValidPassword
  private String password;
}

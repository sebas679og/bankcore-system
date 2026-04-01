package com.bankcore.customers.dto.requests;

import com.bankcore.customers.utils.validators.ValidAtmPin;
import com.bankcore.customers.utils.validators.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) used to capture user registration data.
 *
 * <p>This object represents the request payload received when a new user attempts to register in
 * the system. It includes validation constraints to enforce business rules and input integrity
 * before processing.
 *
 * <p>Validation is performed using Jakarta Bean Validation annotations, ensuring that required
 * fields are present and comply with defined formatting and security rules.
 *
 * @author Bankcore Team
 * @author  Orjuela
 * @version 0.1.0
 */
@Getter
@Setter
@Builder
public class RegisterRequest {

  /**
   * Government-issued identification number of the user.
   *
   * <p>Must not be null or blank.
   */
  @NotBlank(message = "DNI is required")
  private String dni;

  /**
   * User's first name.
   *
   * <p>Must not be null or blank.
   */
  @NotBlank(message = "First name is required")
  private String firstName;

  /**
   * User's last name.
   *
   * <p>Must not be null or blank.
   */
  @NotBlank(message = "Last name is required")
  private String lastName;

  /**
   * User's email address.
   *
   * <p>Must be a valid email format and cannot be null or blank.
   */
  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  /**
   * Account password chosen by the user.
   *
   * <p>Must comply with the custom {@code @ValidPassword} policy, which enforces security
   * requirements such as length, character diversity, and complexity rules.
   */
  @NotBlank(message = "Password is required")
  @ValidPassword
  private String password;

  /**
   * 4-digit ATM PIN associated with the user's account.
   *
   * <p>Must:
   *
   * <ul>
   *   <li>Contain exactly 4 numeric digits
   *   <li>Not be null
   *   <li>Comply with the custom {@code @ValidAtmPin} rule (e.g., disallow sequential or repeated
   *       digits)
   * </ul>
   */
  @NotNull(message = "ATM Pin cannot be null")
  @Size(min = 4, max = 4, message = "ATM Pin must be exactly 4 digits")
  @Pattern(regexp = "\\d+", message = "ATM Pin must contain only numbers")
  @ValidAtmPin
  private String atmPin;

  /**
   * User's contact phone number.
   *
   * <p>Must not be null or blank.
   */
  @NotBlank(message = "Phone number is required")
  private String phone;

  /**
   * Residential address of the user.
   *
   * <p>Must not be null or blank.
   */
  @NotBlank(message = "Address is required")
  private String address;
}

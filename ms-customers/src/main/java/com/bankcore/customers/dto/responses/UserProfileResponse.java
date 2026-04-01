package com.bankcore.customers.dto.responses;

import com.bankcore.customers.utils.enums.CustomerStatus;
import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object representing a customer's profile information.
 *
 * <p>This class is used to send detailed user data to the client after a successful profile
 * retrieval. It includes personal details, contact information, and account metadata.
 *
 * @author BankCore Team - Cristian Ortiz
 * @version 0.1.0
 */
@Data
@Builder
public class UserProfileResponse {

  /** Unique identifier for the user in the system. */
  private String id;

  /** National ID (Documento Nacional de Identidad) of the customer. */
  private String dni;

  /** Customer's first name. */
  private String firstName;

  /** Customer's last name. */
  private String lastName;

  /** Primary email address associated with the account. */
  private String email;

  /** Contact phone number. */
  private String phone;

  /** Physical home or billing address. */
  private String address;

  /**
   * Current status of the user account.
   *
   * @see CustomerStatus
   */
  private String status;

  /** Date string representing when the profile was created. */
  private String createdAt;
}

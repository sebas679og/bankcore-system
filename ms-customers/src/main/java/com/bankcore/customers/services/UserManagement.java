package com.bankcore.customers.services;

import com.bankcore.customers.dto.requests.LoginRequest;
import com.bankcore.customers.dto.requests.PinValidateRequest;
import com.bankcore.customers.dto.requests.RegisterRequest;
import com.bankcore.customers.dto.responses.CustomerDetailsValidateResponse;
import com.bankcore.customers.dto.responses.CustomerValidateResponse;
import com.bankcore.customers.dto.responses.LoginResponse;
import com.bankcore.customers.dto.responses.PinValidateResponse;
import com.bankcore.customers.dto.responses.RegisterResponse;
import com.bankcore.customers.dto.responses.UserProfileResponse;
import com.bankcore.customers.exceptions.ResourceConflictException;
import com.bankcore.customers.exceptions.UserProfileNotFoundException;
import java.util.UUID;

/**
 * Application service interface responsible for managing user-related business operations.
 *
 * <p>This interface defines the contract for user management workflows, such as registration,
 * without exposing implementation details. Implementations are responsible for enforcing business
 * rules, coordinating persistence, and handling security requirements.
 *
 * @author BankCore Team
 * @author Sebastian Orjuela
 * @author Cristian Ortiz
 * @version 0.1.0
 */
public interface UserManagement {

  /**
   * Registers a new user in the system.
   *
   * <p>This operation validates business constraints (e.g., unique DNI and email), encrypts
   * sensitive information such as password and ATM PIN, persists the user entity, and returns a
   * response DTO.
   *
   * @param request the registration request containing user personal and authentication data
   * @return a {@link RegisterResponse} containing non-sensitive information of the registered user
   * @throws ResourceConflictException if a user with the same DNI or email already exists
   * @throws IllegalArgumentException if the request data violates business rules
   */
  RegisterResponse registerCustomer(RegisterRequest request);

  /**
   * Authenticates a user and returns a session token.
   *
   * <p>This method validates the provided credentials and, upon success, issues a JSON Web Token
   * (JWT) for subsequent authenticated requests.
   *
   * @param request the {@link LoginRequest} containing the user's credentials
   * @return a {@link LoginResponse} containing the generated access token and metadata
   * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if the account
   *     associated with the email does not exist
   * @throws org.springframework.security.core.AuthenticationException if the credentials are
   *     invalid or the account is disabled/locked
   */
  LoginResponse login(LoginRequest request);

  /**
   * Retrieves the profile information for a user identified by their id.
   *
   * <p>This method serves as the main entry point for obtaining customer profile details.
   * Implementations are expected to handle validation and ensure the returned data is properly
   * mapped to a response DTO.
   *
   * @param id The unique id of the user.
   * @return A {@link UserProfileResponse} object containing the user's profile details.
   * @throws UserProfileNotFoundException If no user exists with the specified id.
   * @throws IllegalArgumentException If the id parameter is invalid (null or blank).
   */
  UserProfileResponse getCurrentUserProfile(String id);

  /**
   * Retrieve the user details for the accounts service.
   *
   * <p>This method retrieves the user profile and validates its existence, and maps the user to the
   * corresponding DTO
   *
   * @param customerId the unique id of the user.
   * @return A {@link CustomerDetailsValidateResponse} object containing the user's profile details.
   * @throws UserProfileNotFoundException If no user exists with the specified id.
   */
  CustomerDetailsValidateResponse getDetailsCustomer(UUID customerId);

  /**
   * retrieves if the user exists and is active.
   *
   * <p>Retrieve the user by their ID and verify their existence and if their status is {@link
   * com.bankcore.customers.utils.enums.CustomerStatus#ACTIVE}
   *
   * @param customerId the unique id of the user.
   * @return A {@link CustomerValidateResponse} containing the customer ID, existence flag and
   *     active status flag
   */
  CustomerValidateResponse getCustomerIsActive(UUID customerId);

  /**
   * Validates the customer's PIN against the stored credential.
   *
   * <p>The method retrieves the customer's PIN using the provided customer ID and compares it with
   * the PIN received in the request.
   *
   * @param request the request containing the PIN to validate
   * @param customerId the unique identifier of the customer
   * @return a {@link PinValidateResponse} indicating whether the provided PIN is valid
   */
  PinValidateResponse getPinValidateCustomer(PinValidateRequest request, UUID customerId);
}

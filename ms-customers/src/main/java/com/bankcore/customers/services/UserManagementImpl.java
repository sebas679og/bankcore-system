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
import com.bankcore.customers.model.UserEntity;
import com.bankcore.customers.repository.UserRepository;
import com.bankcore.customers.utils.enums.CustomerStatus;
import com.bankcore.customers.utils.enums.UserRole;
import com.bankcore.customers.utils.mappers.UserMapper;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link UserManagement}.
 *
 * <p>This service coordinates the user registration process by:
 *
 * <ul>
 *   <li>Validating uniqueness constraints (DNI and email)
 *   <li>Encrypting sensitive information (password and ATM PIN)
 *   <li>Persisting the user entity
 *   <li>Mapping the persisted entity to a response DTO
 * </ul>
 *
 * <p>Business rules and security requirements are enforced at this layer before interacting with
 * the persistence layer.
 *
 * @author BankCore Team
 * @author Sebastian Orjuela
 * @author Cristian
 * @version 0.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementImpl implements UserManagement {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final UserMapper userMapper;
  private final JwtService jwtService;

  /**
   * Registers a new customer in the system.
   *
   * <p>This method performs the following steps:
   *
   * <ol>
   *   <li>Validates that DNI and email are unique
   *   <li>Hashes password and ATM PIN using {@link PasswordEncoder}
   *   <li>Assigns default role and status
   *   <li>Persists the entity
   *   <li>Returns a non-sensitive response DTO
   * </ol>
   *
   * @param request the registration request containing user data
   * @return a {@link RegisterResponse} containing non-sensitive user information
   * @throws ResourceConflictException if DNI or email already exists
   */
  @Override
  public RegisterResponse registerCustomer(RegisterRequest request) {

    if (userRepository.existsByDni(request.getDni())) {
      throw new ResourceConflictException("DNI already exists");
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ResourceConflictException("Email already exists");
    }

    UserEntity userEntity =
        UserEntity.builder()
            .dni(request.getDni())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .atmPin(passwordEncoder.encode(request.getAtmPin()))
            .phone(request.getPhone())
            .address(request.getAddress())
            .role(UserRole.CUSTOMER)
            .status(CustomerStatus.ACTIVE)
            .build();

    userRepository.save(userEntity);

    return userMapper.toRegisterResponse(userEntity);
  }

  /**
   * Authenticates a user based on email and password and generates a JWT access token.
   *
   * <p>This method performs the following steps:
   *
   * <ol>
   *   <li>Validates the existence of the user by email.
   *   <li>Authenticates the credentials using the {@code AuthenticationManager}.
   *   <li>Updates the {@code SecurityContextHolder} with the authenticated principal.
   *   <li>Generates a new JWT access token and retrieves its expiration.
   * </ol>
   *
   * @param request the {@link LoginRequest} containing the user's email and password
   * @return a {@link LoginResponse} containing the JWT, expiration time, and user details
   * @throws UsernameNotFoundException if no user is found with the provided email
   * @throws org.springframework.security.core.AuthenticationException if authentication fails
   *     (e.g., invalid credentials or locked account)
   */
  @Override
  public LoginResponse login(LoginRequest request) {

    UserEntity userEntity =
        userRepository
            .findByEmailIgnoreCase(request.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));

    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(userEntity.getId(), request.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String jwt = jwtService.generateAccessToken(userDetails);
    long expiresIn = JwtService.ACCESS_TOKEN_EXPIRATION / 1000;

    return userMapper.toLoginResponse(userEntity, jwt, expiresIn);
  }

  /**
   * Retrieves the profile data for a specific user based on their id.
   *
   * <p>This method performs a read-only transaction to fetch the user entity. It validates the
   * input and ensures that if the user does not exist in the persistence layer, a specific business
   * exception is thrown.
   *
   * @param id The id of the authenticated user to retrieve.
   * @return A {@link UserProfileResponse} containing the mapped profile data.
   * @throws IllegalArgumentException If the provided email is null or empty.
   * @throws UserProfileNotFoundException If no user is found with the given email.
   * @see UserEntity
   * @see UserProfileResponse
   */
  @Override
  @Transactional(readOnly = true)
  public UserProfileResponse getCurrentUserProfile(String id) {

    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Id must not be null or blank");
    }

    UserEntity user =
        userRepository
            .findById(UUID.fromString(id))
            .orElseThrow(() -> new UserProfileNotFoundException("Authenticated user not found"));

    return userMapper.toUserProfileResponse(user);
  }

  /**
   * Retrieves detailed information about a customer based on their unique identifier.
   *
   * <p>This method queries the {@link UserRepository} to find a {@link UserEntity} associated with
   * the provided customer ID. If no entity is found, a {@link UserProfileNotFoundException} is
   * thrown. The retrieved entity is then mapped into a {@link CustomerDetailsValidateResponse}
   * using the {@link UserMapper}.
   *
   * @param customerId the unique identifier of the customer; must not be null
   * @return a {@link CustomerDetailsValidateResponse} containing the validated customer details
   * @throws UserProfileNotFoundException if no customer exists with the provided ID
   * @see UserEntity
   * @see CustomerDetailsValidateResponse
   * @see UserRepository
   * @see UserMapper
   */
  @Override
  @Transactional(readOnly = true)
  public CustomerDetailsValidateResponse getDetailsCustomer(UUID customerId) {

    log.info("Fetching customer for details by ID: {}", customerId);

    UserEntity customer =
        userRepository
            .findById(customerId)
            .orElseThrow(
                () -> {
                  log.warn("Customer not found in details. ID: {}", customerId);
                  return new UserProfileNotFoundException(
                      "There is no client with the provided ID");
                });

    return userMapper.toCustomerDetailsValidateResponse(customer);
  }

  /**
   * Validates whether a customer exists and is currently active.
   *
   * <p>This method attempts to retrieve a {@link UserEntity} from the {@link UserRepository} using
   * the provided customer ID. It then checks if the customer exists and whether their status is
   * {@link CustomerStatus#ACTIVE}. The result is encapsulated in a {@link
   * CustomerValidateResponse}, which indicates both existence and active state.
   *
   * <p>Logging is performed to trace the validation process.
   *
   * @param customerId the unique identifier of the customer; must not be null
   * @return a {@link CustomerValidateResponse} containing the customer ID, existence flag, and
   *     active status flag
   * @see UserEntity
   * @see CustomerValidateResponse
   * @see CustomerStatus
   * @see UserRepository
   */
  @Override
  @Transactional(readOnly = true)
  public CustomerValidateResponse getCustomerIsActive(UUID customerId) {

    log.info("Fetching customer for validation by ID: {}", customerId);

    Optional<UserEntity> optionalCustomer = userRepository.findById(customerId);

    boolean exists = optionalCustomer.isPresent();
    boolean isActive = exists && CustomerStatus.ACTIVE.equals(optionalCustomer.get().getStatus());

    return CustomerValidateResponse.builder()
        .customerId(customerId)
        .exists(exists)
        .active(isActive)
        .build();
  }

  /**
   * Validates the ATM PIN provided by a customer.
   *
   * <p>This method retrieves the user associated with the given {@code customerId} and compares the
   * provided PIN with the encrypted PIN stored in the database using {@link
   * org.springframework.security.crypto.password.PasswordEncoder#matches(CharSequence, String)}.
   *
   * <p>The comparison is performed securely against the encoded PIN, ensuring that the raw PIN
   * value is never stored or compared in plain text.
   *
   * @param request the request containing the PIN to be validated.
   * @param customerId the unique identifier of the customer whose PIN will be validated.
   * @return a {@link PinValidateResponse} indicating whether the provided PIN matches the stored
   *     encoded PIN.
   * @throws UserProfileNotFoundException if no user exists with the provided {@code customerId}.
   */
  @Override
  @Transactional(readOnly = true)
  public PinValidateResponse getPinValidateCustomer(PinValidateRequest request, UUID customerId) {

    log.info("Obtaining customer pin for validation: {}", customerId);

    UserEntity user =
        userRepository
            .findById(customerId)
            .orElseThrow(() -> new UserProfileNotFoundException("User not found"));

    boolean isValid = passwordEncoder.matches(request.getPin(), user.getAtmPin());

    return PinValidateResponse.builder().valid(isValid).build();
  }
}

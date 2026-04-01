package com.bankcore.customers;

import com.bankcore.customers.dto.requests.PinValidateRequest;
import com.bankcore.customers.model.UserEntity;
import com.bankcore.customers.utils.enums.CustomerStatus;
import com.bankcore.customers.utils.enums.UserRole;

/**
 * Utility class that provides mock {@link UserEntity} instances for testing purposes.
 *
 * <p>This class centralizes the creation of sample users with predefined attributes such as name,
 * email, role, and status. It ensures consistency across test scenarios by abstracting user
 * construction and reducing boilerplate code.
 *
 * <p>Typical usage includes simulating active customers or validating authentication and
 * authorization flows with predictable mock data.
 *
 * @author BankcoreTeam
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
public class DataProvider {
  public static final String EMAIL = "juan@test.com";
  public static final String UUID = "9e85d91b-3b89-4404-b0ca-12a4b0533510";
  public static final String INVALID_UUID = "550e8400-e29b-41d4-a716446655440000";
  public static final String CUSTOMER_ROLE = "CUSTOMER";
  public static final String ADMIN_ROLE = "ADMIN";
  public static final String SERVICE_ROLE = "SERVICE";

  /**
   * Creates a mock {@link UserEntity} with default attributes.
   *
   * <p>The user is initialized with fixed personal details, credentials, and an active customer
   * role. This method is useful for testing login, registration, or role-based scenarios without
   * requiring real user data.
   *
   * @return a mock {@link UserEntity} representing an active customer
   */
  public static UserEntity createMockUser() {
    return UserEntity.builder()
        .dni("12345678")
        .firstName("Juan")
        .lastName("Perez")
        .email("juan@test.com")
        .password("Password123!")
        .atmPin("1234")
        .phone("3001234567")
        .address("Bogotá")
        .role(UserRole.CUSTOMER)
        .status(CustomerStatus.ACTIVE)
        .build();
  }

  public static PinValidateRequest createMockPinValidate(String pin) {
    return PinValidateRequest.builder().pin(pin).build();
  }
}

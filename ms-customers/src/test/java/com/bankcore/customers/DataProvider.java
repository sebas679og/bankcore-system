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

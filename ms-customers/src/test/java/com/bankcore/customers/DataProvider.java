package com.bankcore.customers;

import com.bankcore.customers.dto.requests.PinValidateRequest;
import com.bankcore.customers.model.UserEntity;
import com.bankcore.customers.utils.enums.CustomerStatus;
import com.bankcore.customers.utils.enums.UserRole;

public class DataProvider {

  public static final String EMAIL = "juan@test.com";
  public static final String UUID = "9e85d91b-3b89-4404-b0ca-12a4b0533510";
  public static final String INVALID_UUID = "550e8400-e29b-41d4-a716446655440000";
  public static final String CUSTOMER_ROLE = "CUSTOMER";
  public static final String ADMIN_ROLE = "ADMIN";
  public static final String SERVICE_ROLE = "SERVICE";

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

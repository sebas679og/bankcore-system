package com.bankcore.customers.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bankcore.customers.dto.requests.LoginRequest;
import com.bankcore.customers.dto.requests.RegisterRequest;
import com.bankcore.customers.dto.responses.LoginResponse;
import com.bankcore.customers.dto.responses.RegisterResponse;
import com.bankcore.customers.services.UserManagement;
import com.bankcore.customers.utils.enums.CustomerStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Unit tests for {@link UserController}. */
@ExtendWith(MockitoExtension.class)
class UserControllerUnitTest {

  @Mock private UserManagement userManagement;

  @InjectMocks private UserController userController;

  @Test
  void register_Customer_Success() {
    RegisterRequest request =
        RegisterRequest.builder()
            .dni("1234567890")
            .firstName("John")
            .lastName("Doe")
            .email("johndoe@email.com")
            .password("Password123!")
            .atmPin("1234")
            .phone("+573001234567")
            .address("123 Main St")
            .build();

    RegisterResponse expectedResponse =
        RegisterResponse.builder()
            .id(java.util.UUID.randomUUID())
            .dni(request.getDni())
            .fullName(request.getFirstName() + " " + request.getLastName())
            .email(request.getEmail())
            .status(CustomerStatus.ACTIVE)
            .createdDate(java.time.Instant.now())
            .build();
    when(userManagement.registerCustomer(request)).thenReturn(expectedResponse);
    ResponseEntity<RegisterResponse> response = userController.register(request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(expectedResponse, response.getBody());
    verify(userManagement, times(1)).registerCustomer(request);
  }

  @Test
  void shouldReturn200_whenCredentialsAreValid() {
    LoginRequest request =
        LoginRequest.builder().email("customer@bankcore.com").password("Secure@123").build();

    LoginResponse response =
        LoginResponse.builder()
            .token("token")
            .tokenType("Bearer")
            .expiresIn(3600000L)
            .customerId("uuid")
            .build();

    when(userManagement.login(any())).thenReturn(response);

    ResponseEntity<LoginResponse> result = userController.login(request);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(response);
    verify(userManagement).login(request);
  }
}

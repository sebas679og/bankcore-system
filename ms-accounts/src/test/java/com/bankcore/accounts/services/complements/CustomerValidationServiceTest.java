package com.bankcore.accounts.services.complements;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bankcore.accounts.exceptions.CustomerInactiveException;
import com.bankcore.accounts.exceptions.CustomerNotFoundException;
import com.bankcore.accounts.integrations.client.CustomerClient;
import com.bankcore.accounts.integrations.dto.request.PinValidateRequest;
import com.bankcore.accounts.integrations.dto.responses.CustomerResponse;
import com.bankcore.accounts.integrations.dto.responses.PinValidateResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CustomerValidationServiceTest {

  @Mock private CustomerClient customerClient;

  @InjectMocks private CustomerValidationService customerValidationService;

  private UUID customerId;

  @BeforeEach
  void setUp() {
    customerId = UUID.randomUUID();
  }

  @Test
  void shouldThrowCustomerNotFound_whenCustomerDoesNotExist() {
    CustomerResponse response = new CustomerResponse(customerId, false, false);

    when(customerClient.getCustomerById(customerId)).thenReturn(response);

    assertThrows(
        CustomerNotFoundException.class,
        () -> customerValidationService.validateCustomerIsActive(customerId));
  }

  @Test
  void shouldThrowCustomerInactive_whenCustomerIsInactive() {
    CustomerResponse response = new CustomerResponse(customerId, true, false);

    when(customerClient.getCustomerById(customerId)).thenReturn(response);

    assertThrows(
        CustomerInactiveException.class,
        () -> customerValidationService.validateCustomerIsActive(customerId));
  }

  @Test
  void shouldPass_whenCustomerIsActive() {
    CustomerResponse response = new CustomerResponse(customerId, true, true);

    when(customerClient.getCustomerById(customerId)).thenReturn(response);

    assertDoesNotThrow(() -> customerValidationService.validateCustomerIsActive(customerId));
  }

  @Test
  void shouldReturnPinValidateResponse_whenValidateCustomerPinIsCalled() {
    PinValidateRequest request = PinValidateRequest.builder().pin("1234").build();
    PinValidateResponse expectedResponse = new PinValidateResponse(true);

    when(customerClient.validateCustomerPin(customerId, request)).thenReturn(expectedResponse);

    PinValidateResponse response =
        customerValidationService.validateCustomerPin(customerId, request);

    assertNotNull(response);
    assertEquals(expectedResponse, response);

    verify(customerClient).validateCustomerPin(customerId, request);
  }
}

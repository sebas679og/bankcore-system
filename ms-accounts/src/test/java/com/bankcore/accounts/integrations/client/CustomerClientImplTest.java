package com.bankcore.accounts.integrations.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bankcore.accounts.exceptions.CustomExternalServiceException;
import com.bankcore.accounts.exceptions.CustomInternalServiceException;
import com.bankcore.accounts.integrations.dto.request.PinValidateRequest;
import com.bankcore.accounts.integrations.dto.responses.CustomerDetailsResponse;
import com.bankcore.accounts.integrations.dto.responses.CustomerResponse;
import com.bankcore.accounts.integrations.dto.responses.PinValidateResponse;
import com.bankcore.accounts.utils.enums.CustomerStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link CustomerClientImpl} using Mockito to mock WebClient interactions. Tests
 * cover
 */
@ExtendWith(MockitoExtension.class)
public class CustomerClientImplTest {

  @Mock private WebClient webClient;

  @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock private WebClient.RequestBodyUriSpec requestBodyUriSpec;

  @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;

  @Mock private WebClient.RequestBodySpec requestBodySpec;

  @Mock private WebClient.ResponseSpec responseSpec;

  @InjectMocks private CustomerClientImpl customerClient;

  private UUID customerId;

  @BeforeEach
  void setUp() {
    customerId = UUID.randomUUID();
  }

  @Test
  void shouldReturnCustomerResponse_when200() {

    CustomerResponse response = new CustomerResponse(customerId, true, true);

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri("/api/customers/{id}/validate", customerId))
        .thenReturn(requestHeadersSpec);

    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

    when(responseSpec.bodyToMono(CustomerResponse.class)).thenReturn(Mono.just(response));

    CustomerResponse result = customerClient.getCustomerById(customerId);

    assertNotNull(result);
    assertEquals(customerId, result.customerId());
    assertTrue(result.exists());
  }

  @Test
  void shouldReturnPinValidationResponse_when200() {

    PinValidateRequest request = PinValidateRequest.builder().pin("1234").build();

    PinValidateResponse response = new PinValidateResponse(true);

    when(webClient.post()).thenReturn(requestBodyUriSpec);

    when(requestBodyUriSpec.uri("/api/customers/{id}/validate-pin", customerId))
        .thenReturn(requestBodySpec);

    when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);

    when(requestBodySpec.bodyValue(request)).thenReturn(requestHeadersSpec);

    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

    when(responseSpec.bodyToMono(PinValidateResponse.class)).thenReturn(Mono.just(response));

    PinValidateResponse result = customerClient.validateCustomerPin(customerId, request);

    assertNotNull(result);
    assertTrue(result.valid());
  }

  @Test
  void shouldReturnCustomerDetailsResponse_when200() {

    CustomerDetailsResponse response =
        new CustomerDetailsResponse(
            customerId, "12345781", "John Doe", "johndoe@email.com", CustomerStatus.ACTIVE);

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri("/api/customers/{id}", customerId))
        .thenReturn(requestHeadersSpec);

    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

    when(responseSpec.bodyToMono(CustomerDetailsResponse.class)).thenReturn(Mono.just(response));

    CustomerDetailsResponse result = customerClient.getCustomerDetailsById(customerId);

    assertNotNull(result);
    assertEquals(response, result);
  }

  @Test
  void shouldThrowInternalException_when4xx() {

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(UUID.class))).thenReturn(requestHeadersSpec);

    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

    when(responseSpec.bodyToMono(CustomerResponse.class))
        .thenReturn(Mono.error(new CustomInternalServiceException("error")));

    assertThrows(
        CustomInternalServiceException.class, () -> customerClient.getCustomerById(customerId));
  }

  @Test
  void shouldThrowExternalException_when5xx() {

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(UUID.class))).thenReturn(requestHeadersSpec);

    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

    when(responseSpec.bodyToMono(CustomerResponse.class))
        .thenReturn(Mono.error(new CustomExternalServiceException("error")));

    assertThrows(
        CustomExternalServiceException.class, () -> customerClient.getCustomerById(customerId));
  }

  @Test
  void shouldThrowInternalException_whenBodyIsEmpty() {

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(UUID.class))).thenReturn(requestHeadersSpec);

    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

    when(responseSpec.bodyToMono(CustomerResponse.class)).thenReturn(Mono.empty());

    assertThrows(
        CustomInternalServiceException.class, () -> customerClient.getCustomerById(customerId));
  }

  @Test
  void shouldExecute4xxLambda() {

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(UUID.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

    List<Function<ClientResponse, Mono<? extends Throwable>>> capturedHandlers = new ArrayList<>();

    when(responseSpec.onStatus(any(), any()))
        .thenAnswer(
            invocation -> {
              capturedHandlers.add(invocation.getArgument(1));
              return responseSpec;
            });

    when(responseSpec.bodyToMono(CustomerResponse.class))
        .thenReturn(Mono.just(mock(CustomerResponse.class)));

    customerClient.getCustomerById(customerId);

    ClientResponse response4xx = mock(ClientResponse.class);
    when(response4xx.statusCode()).thenReturn(HttpStatus.BAD_REQUEST);
    when(response4xx.bodyToMono(String.class)).thenReturn(Mono.just("bad request"));

    assertThrows(
        CustomInternalServiceException.class,
        () -> capturedHandlers.get(0).apply(response4xx).block());
  }

  @Test
  void shouldExecute5xxLambda() {

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(UUID.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

    List<Function<ClientResponse, Mono<? extends Throwable>>> capturedHandlers = new ArrayList<>();

    when(responseSpec.onStatus(any(), any()))
        .thenAnswer(
            invocation -> {
              capturedHandlers.add(invocation.getArgument(1));
              return responseSpec;
            });

    when(responseSpec.bodyToMono(CustomerResponse.class))
        .thenReturn(Mono.just(mock(CustomerResponse.class)));

    customerClient.getCustomerById(customerId);

    ClientResponse response5xx = mock(ClientResponse.class);
    when(response5xx.statusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
    when(response5xx.bodyToMono(String.class)).thenReturn(Mono.just("server error"));

    assertThrows(
        CustomExternalServiceException.class,
        () -> capturedHandlers.get(1).apply(response5xx).block());
  }
}

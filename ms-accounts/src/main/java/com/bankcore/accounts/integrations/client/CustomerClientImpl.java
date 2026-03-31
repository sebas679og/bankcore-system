package com.bankcore.accounts.integrations.client;

import com.bankcore.accounts.exceptions.CustomExternalServiceException;
import com.bankcore.accounts.exceptions.CustomInternalServiceException;
import com.bankcore.accounts.integrations.dto.request.PinValidateRequest;
import com.bankcore.accounts.integrations.dto.responses.CustomerDetailsResponse;
import com.bankcore.accounts.integrations.dto.responses.CustomerResponse;
import com.bankcore.accounts.integrations.dto.responses.PinValidateResponse;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Implementation of the {@link CustomerClient} interface that uses {@link WebClient} to communicate
 * with the Customer service.
 *
 * <p>This class provides concrete implementations for retrieving customer information and
 * validating customer PINs. It leverages Spring's {@link WebClient} for asynchronous HTTP requests
 * and includes robust error handling for both client-side (4xx) and server-side (5xx) scenarios.
 *
 * <h2>Error Handling:</h2>
 *
 * <ul>
 *   <li>4xx errors: Logged and wrapped into {@link CustomInternalServiceException}.
 *   <li>5xx errors: Logged and wrapped into {@link CustomExternalServiceException}.
 *   <li>Empty responses: Logged and wrapped into {@link CustomInternalServiceException}.
 * </ul>
 *
 * @author BankCore Team - Sebastian Orjuela
 * @version 1.1
 */
@Slf4j
@Component
@AllArgsConstructor
public class CustomerClientImpl implements CustomerClient {

  private final WebClient customersWebClient;

  /**
   * Retrieves customer information by unique identifier.
   *
   * @param customerId the {@link UUID} representing the customer's unique ID
   * @return a {@link CustomerResponse} containing customer details
   * @throws CustomInternalServiceException if the Customer service rejects the request or returns
   *     an empty body
   * @throws CustomExternalServiceException if the Customer service is unavailable
   */
  @Override
  public CustomerResponse getCustomerById(UUID customerId) {
    WebClient.RequestHeadersSpec<?> request =
        customersWebClient.get().uri("/api/customers/{id}/validate", customerId);

    return executeRequest(request, CustomerResponse.class, customerId);
  }

  /**
   * Validates the customer's PIN against the provided request.
   *
   * @param customerId the {@link UUID} representing the customer's unique ID
   * @param request the {@link PinValidateRequest} containing the PIN to validate
   * @return a {@link PinValidateResponse} indicating whether the PIN is valid
   * @throws CustomInternalServiceException if the Customer service rejects the request or returns
   *     an empty body
   * @throws CustomExternalServiceException if the Customer service is unavailable
   */
  @Override
  public PinValidateResponse validateCustomerPin(UUID customerId, PinValidateRequest request) {
    WebClient.RequestHeadersSpec<?> req =
        customersWebClient
            .post()
            .uri("/api/customers/{id}/validate-pin", customerId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request);

    return executeRequest(req, PinValidateResponse.class, customerId);
  }

  /**
   * Get the customer details by their id
   *
   * @param customerId the {@link UUID} representing the customer's unique ID
   * @return a {@link CustomerDetailsResponse} contains the detailed information of the client
   * @throws CustomInternalServiceException if the Customer service rejects the request or returns
   *     an empty body
   * @throws CustomExternalServiceException if the Customer service is unavailable
   */
  @Override
  public CustomerDetailsResponse getCustomerDetailsById(UUID customerId) {
    WebClient.RequestHeadersSpec<?> request =
        customersWebClient.get().uri("/api/customers/{id}", customerId);

    return executeRequest(request, CustomerDetailsResponse.class, customerId);
  }

  /**
   * Executes a {@link WebClient} request and handles error scenarios.
   *
   * @param request the prepared {@link WebClient.RequestHeadersSpec} request
   * @param responseType the expected response type
   * @param customerId the {@link UUID} of the customer for logging context
   * @param <T> the type of the response object
   * @return the response body mapped to the specified type
   * @throws CustomInternalServiceException if the Customer service rejects the request or returns
   *     an empty body
   * @throws CustomExternalServiceException if the Customer service is unavailable
   */
  private <T> T executeRequest(
      WebClient.RequestHeadersSpec<?> request, Class<T> responseType, UUID customerId) {
    return request
        .retrieve()
        .onStatus(
            HttpStatusCode::is4xxClientError,
            response ->
                response
                    .bodyToMono(String.class)
                    .defaultIfEmpty("No body")
                    .flatMap(
                        body -> {
                          log.error(
                              "Customer Service 4xx error. ID: {}, Status: {}, Body: {}",
                              customerId,
                              response.statusCode(),
                              body);
                          return Mono.error(
                              new CustomInternalServiceException(
                                  "Customer Service rejected the request"));
                        }))
        .onStatus(
            HttpStatusCode::is5xxServerError,
            response ->
                response
                    .bodyToMono(String.class)
                    .defaultIfEmpty("No body")
                    .flatMap(
                        body -> {
                          log.error(
                              "Customer Service 5xx error. ID: {}, Status: {}, Body: {}",
                              customerId,
                              response.statusCode(),
                              body);
                          return Mono.error(
                              new CustomExternalServiceException(
                                  "Customer Service is unavailable"));
                        }))
        .bodyToMono(responseType)
        .blockOptional()
        .orElseThrow(
            () -> {
              log.error("Customer Service returned empty body. ID: {}", customerId);
              return new CustomInternalServiceException("Empty response from Customer Service");
            });
  }
}

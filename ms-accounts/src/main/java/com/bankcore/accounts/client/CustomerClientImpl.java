package com.bankcore.accounts.client;

import java.util.UUID;

import com.bankcore.accounts.exceptions.CustomerInactiveException;
import com.bankcore.accounts.exceptions.CustomerNotFoundException;
import org.springframework.http.HttpStatusCode;
import com.bankcore.accounts.dto.responses.CustomerResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.bankcore.accounts.exceptions.CustomExternalServiceException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Implementation of the CustomerClient interface that uses WebClient to communicate with the Customer service.
 * This class handles the retrieval of customer information and includes error handling for various scenarios.
 * @author BankCore Team - Sebastian Orjuela - Cristian Ortiz
 * @version 1.0
 */
@Slf4j
@Component
@AllArgsConstructor
public class CustomerClientImpl implements CustomerClient {

    private final WebClient customersWebClient;

    @Override
    public CustomerResponse getCustomerById(UUID id) {


        CustomerResponse response = customersWebClient
                .get()
                .uri("/api/customers/{id}/validate", id.toString())
                .header("X-Internal-Service", "ms-accounts")
                .retrieve()
                .onStatus(status -> status.value() == 404, clientResponse -> {
                    log.warn("Customer not found in Customer Service. ID: {}", id);
                    return Mono.error(
                            new CustomerNotFoundException("The authenticated client is not registered")
                    );
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("No body")
                                .flatMap(body -> {
                                    log.error(
                                            "5xx error from Customer Service. ID: {}, Status: {}, Body: {}",
                                            id,
                                            clientResponse.statusCode(),
                                            body
                                    );
                                    return Mono.error(
                                            new CustomExternalServiceException(
                                                    "Error communicating with Customer Service"
                                            )
                                    );
                                })
                )
                .bodyToMono(CustomerResponse.class)
                .block();

        if (response == null) {
            log.error("Customer Service returned empty body. ID: {}", id);
            throw new CustomExternalServiceException("Empty response from Customer Service");
        }

        if (!response.exists()) {
            log.warn("Customer does not exist according to validation endpoint. ID: {}", id);
            throw new CustomerNotFoundException("The authenticated client is not registered");
        }

        if (!response.isActive()) {
            throw new CustomerInactiveException("You do not have permission to access this resource.");
        }

        return response;
    }
}

package com.bankcore.accounts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for creating a {@link WebClient} instance used to communicate with the
 * ms-customers microservice.
 *
 * <p>The client automatically attaches a service-level JWT token to each outgoing request for
 * internal authentication.
 *
 * @author BankCore Team - Sebastian Orjuela
 * @version 0.1.0
 */
@Configuration
public class WebClientConfig {

  /**
   * Creates a {@link WebClient} bean configured to communicate with the ms-customers microservice.
   *
   * <p>The base URL for the ms-customers service is injected from application properties, and a
   * filter is added to attach a service-level JWT token to each request for authentication.
   *
   * @param customersUrl the base URL of the ms-customers microservice
   * @param tokenProvider the service responsible for generating JWT tokens for authentication
   * @return a configured {@link WebClient} instance
   */
  @Bean
  public WebClient customersWebClient(
      @Value("${ms-customers.url}") String customersUrl, JwtTokenProviderService tokenProvider) {

    return WebClient.builder()
        .baseUrl(customersUrl)
        .filter(
            (request, next) -> {
              ClientRequest newRequest =
                  ClientRequest.from(request)
                      .headers(
                          headers -> headers.setBearerAuth(tokenProvider.generateServiceToken()))
                      .build();

              return next.exchange(newRequest);
            })
        .build();
  }
}

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
 * @version 1.0
 */
@Configuration
public class WebClientConfig {

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

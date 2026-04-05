package com.bankcore.accounts.integrations.client;

import com.bankcore.accounts.integrations.dto.request.PinValidateRequest;
import com.bankcore.accounts.integrations.dto.responses.CustomerDetailsResponse;
import com.bankcore.accounts.integrations.dto.responses.CustomerResponse;
import com.bankcore.accounts.integrations.dto.responses.PinValidateResponse;
import java.util.UUID;

/**
 * Client interface for interacting with the Customer service.
 *
 * <p>This interface defines methods for retrieving customer information and validating customer
 * PINs. It acts as a contract between the application and the Customer service, ensuring consistent
 * request and response handling.
 *
 * @author BankCore Team - Sebastian Orjuela - Cristian Ortiz
 * @version 0.2.0
 */
public interface CustomerClient {

  /**
   * Retrieves customer information based on the unique identifier.
   *
   * @param customerId the {@link UUID} representing the customer's unique ID
   * @return a {@link CustomerResponse} containing customer details
   */
  CustomerResponse getCustomerById(UUID customerId);

  /**
   * Validates the customer's PIN against the provided request.
   *
   * @param customerId the {@link UUID} representing the customer's unique ID
   * @param request the {@link PinValidateRequest} containing the PIN to validate
   * @return a {@link PinValidateResponse} indicating whether the PIN is valid
   */
  PinValidateResponse validateCustomerPin(UUID customerId, PinValidateRequest request);

  /**
   * Get the customer details by their id.
   *
   * @param customerId the {@link UUID} representing the customer's unique ID
   * @return a {@link CustomerDetailsResponse} contains the detailed information of the client
   */
  CustomerDetailsResponse getCustomerDetailsById(UUID customerId);
}

package com.bankcore.accounts.services.complements;

import com.bankcore.accounts.exceptions.CustomerInactiveException;
import com.bankcore.accounts.exceptions.CustomerNotFoundException;
import com.bankcore.accounts.integrations.client.CustomerClient;
import com.bankcore.accounts.integrations.dto.request.PinValidateRequest;
import com.bankcore.accounts.integrations.dto.responses.CustomerDetailsResponse;
import com.bankcore.accounts.integrations.dto.responses.CustomerResponse;
import com.bankcore.accounts.integrations.dto.responses.PinValidateResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service responsible for validating customer state before allowing transactional or business
 * operations.
 *
 * <p>This service interacts with the {@link CustomerClient} to retrieve customer information and
 * ensures that the customer exists and is active before proceeding with business logic. It also
 * delegates PIN validation requests to the Customer service.
 *
 * @author BankcoreTeam - Sebastian Orjuela
 * @version 0.1.0
 */
@Service
@RequiredArgsConstructor
public class CustomerValidationService {

  private final CustomerClient customerClient;

  /**
   * Validates if the customer associated with the given ID is active.
   *
   * <p>If the customer does not exist, a {@link CustomerNotFoundException} is thrown. If the
   * customer exists but is inactive, a {@link CustomerInactiveException} is thrown.
   *
   * @param idCustomer the {@link UUID} of the customer to validate
   * @throws CustomerNotFoundException if the customer does not exist in the system
   * @throws CustomerInactiveException if the customer exists but is not active
   */
  public void validateCustomerIsActive(UUID idCustomer) {
    CustomerResponse customer = customerClient.getCustomerById(idCustomer);
    if (!customer.exists()) {
      throw new CustomerNotFoundException("The customer is not registered in the system");
    }

    if (!customer.isActive()) {
      throw new CustomerInactiveException("The authenticated client is not active");
    }
  }

  /**
   * Validates the customer's PIN by delegating the request to the {@link CustomerClient}.
   *
   * @param customerId the {@link UUID} representing the customer's unique ID
   * @param request the {@link PinValidateRequest} containing the PIN to validate
   * @return a {@link PinValidateResponse} indicating whether the PIN is valid
   */
  public PinValidateResponse validateCustomerPin(UUID customerId, PinValidateRequest request) {
    return customerClient.validateCustomerPin(customerId, request);
  }

  /**
   * Retrieves the full name of a customer by their unique identifier.
   *
   * <p>This method delegates to the {@code customerClient} to fetch customer details and extracts
   * the {@code fullName} field from the response.
   *
   * @param customerId unique identifier of the customer
   * @return the full name of the customer associated with the given ID
   * @throws NullPointerException if the customer details cannot be retrieved
   */
  public String getFullNameCustomerById(UUID customerId) {
    CustomerDetailsResponse customerDetails = customerClient.getCustomerDetailsById(customerId);
    return customerDetails.fullName();
  }
}

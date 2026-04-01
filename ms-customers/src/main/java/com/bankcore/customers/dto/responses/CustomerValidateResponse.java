package com.bankcore.customers.dto.responses;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO class with response schema for the internal service query, validation of active and existing.
 * client in the system
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 0.1.0
 */
@Builder
@Getter
@AllArgsConstructor
public class CustomerValidateResponse {

  private UUID customerId;
  private boolean exists;
  private boolean active;
}

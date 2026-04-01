package com.bankcore.accounts.integrations.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * Response DTO for customer information.
 *
 * @author BankCore Team - Sebastian Orjuela
 * @version 0.1.0
 */
public record CustomerResponse(
    UUID customerId, boolean exists, @JsonProperty("active") boolean isActive) {}

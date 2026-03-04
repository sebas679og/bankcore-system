package com.bankcore.accounts.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Response DTO for customer information.
 *
 * @author BankCore Team - Sebastian Orjuela - Cristian Ortiz
 * @version 1.0
 */
public record CustomerResponse(UUID customerId,
                               @JsonProperty("exist") boolean exists,
                               @JsonProperty("active") boolean isActive) {
}

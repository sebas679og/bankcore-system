package com.bankcore.accounts.integrations.dto.responses;

import com.bankcore.accounts.utils.enums.CustomerStatus;
import java.util.UUID;

/**
 * Represents the response payload containing customer details.
 *
 * <p>This Data Transfer Object (DTO) is used to expose customer information in API responses,
 * including identifiers, personal data, and current status.
 *
 * @author BankcoreTeam - Sebastian Orjuela
 * @version 0.1.0
 */
public record CustomerDetailsResponse(
    UUID id, String dni, String fullName, String email, CustomerStatus status) {}

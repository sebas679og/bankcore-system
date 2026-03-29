package com.bankcore.accounts.dto.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) representing a request to create a new financial transaction.
 * <p>
 * This class is mutable and built using Lombok's {@link Getter}, {@link Setter}, and {@link Builder}
 * annotations. It encapsulates the input data required by service or controller layers
 * to process a transaction creation request.
 * </p>
 *
 * <p>
 * Fields:
 * <ul>
 *   <li>{@code amount} – Monetary value of the transaction. Must not be {@code null} and must be at least 1.0.</li>
 *   <li>{@code description} – Optional human-readable description or concept of the transaction.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Validation:
 * <ul>
 *   <li>{@code @NotNull} ensures that {@code amount} is always provided.</li>
 *   <li>{@code @DecimalMin("1.0")} enforces a minimum transaction amount of 1.0.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Usage:
 * <ul>
 *   <li>Used in REST API endpoints to capture client input for transaction creation.</li>
 *   <li>Ensures that invalid requests are rejected early through bean validation.</li>
 *   <li>Can be easily serialized/deserialized to and from JSON.</li>
 * </ul>
 * </p>
 *
 * @author BankcoreTeam - Sebastian Orjuela
 * @version 1.0
 */

@Getter
@Setter
@Builder
public class TransactionRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least 1.0")
    private BigDecimal amount;

    private String description;

    @NotNull(message = "PIN cannot be null")
    @NotBlank(message = "PIN cannot be empty")
    @Pattern(regexp = "\\d{4}", message = "PIN must be exactly 4 numeric digits")
    private String pin;
}

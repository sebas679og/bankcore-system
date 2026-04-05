package com.bankcore.accounts.dto.responses;

import com.bankcore.accounts.utils.enums.TransactionStatus;
import com.bankcore.accounts.utils.enums.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/**
 * Data Transfer Object (DTO) representing the response details of a financial transaction.
 *
 * <p>This class is immutable and built using Lombok's {@link Builder} and {@link Value}
 * annotations. It provides a structured view of transaction information for API clients, ensuring
 * clarity and consistency in responses.
 *
 * <p>Fields:
 *
 * <ul>
 *   <li>{@code referenceNumber} – Unique identifier assigned to the transaction.
 *   <li>{@code type} – Type of transaction (e.g., DEPOSIT, WITHDRAWAL).
 *   <li>{@code amount} – Monetary value of the transaction.
 *   <li>{@code balanceBefore} – Account balance before the transaction was applied.
 *   <li>{@code balanceAfter} – Account balance immediately after the transaction.
 *   <li>{@code description} – Human-readable description or concept of the transaction.
 *   <li>{@code timestamp} – Exact time when the transaction occurred.
 *   <li>{@code status} – Current status of the transaction (e.g., COMPLETED, PENDING).
 * </ul>
 *
 * <p>Usage:
 *
 * <ul>
 *   <li>Returned by service or controller layers to expose transaction details to clients.
 *   <li>Ensures immutability and thread-safety for response objects.
 *   <li>Can be easily serialized to JSON for REST APIs.
 * </ul>
 *
 * @author BankcoreTeam - Sebastian Orjuela
 * @version 0.1.0
 */
@Value
@Builder
public class TransactionResponse {

  String referenceNumber;
  TransactionType type;
  BigDecimal amount;
  BigDecimal balanceBefore;
  BigDecimal balanceAfter;
  String description;
  Instant timestamp;
  TransactionStatus status;
}

package com.bankcore.accounts.dto.responses;

import com.bankcore.accounts.utils.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * {@code TransactionHistoryResponse} is an immutable Data Transfer Object (DTO) that represents the
 * details of a single transaction in a user's history.
 *
 * <p>This class is typically used as an API response model to provide clients with structured
 * information about transactions, including type, amount, balance, and counterparty details.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Expose transaction attributes in a read-only format.
 *   <li>Provide contextual information such as description and timestamp.
 *   <li>Ensure immutability through Lombok's {@link lombok.Value} annotation.
 *   <li>Support builder pattern via {@link lombok.Builder} for easy instantiation.
 *   <li>Control JSON serialization with {@link com.fasterxml.jackson.annotation.JsonInclude}:
 *       <ul>
 *         <li>Exclude {@code null} fields from JSON output.
 *         <li>Always include {@code description}, even if {@code null}.
 *       </ul>
 * </ul>
 *
 * <p>This object is returned by APIs to provide clients with transaction history details in a
 * consistent, immutable, and JSON-friendly format.
 *
 * @author BankcoreTeam
 * @author Sebastian Orjuela
 * @version 0.2.0
 * @see TransactionType
 */
@Value
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Jacksonized
public class TransactionHistoryResponse {

  /** Unique reference number of the transaction. */
  String referenceNumber;

  /** The type of transaction (e.g., DEBIT, CREDIT). */
  TransactionType type;

  /** The monetary amount of the transaction. */
  BigDecimal amount;

  /** The account balance after the transaction. */
  BigDecimal balance;

  /**
   * A textual description of the transaction. Always included in JSON output, even if {@code null}.
   */
  @JsonInclude(JsonInclude.Include.ALWAYS)
  String description;

  /** The account number of the counterparty involved in the transaction. */
  String counterpartyAccountNumber;

  /** The name of the counterparty involved in the transaction. */
  String counterpartyName;

  /** The timestamp when the transaction occurred. */
  Instant timestamp;
}

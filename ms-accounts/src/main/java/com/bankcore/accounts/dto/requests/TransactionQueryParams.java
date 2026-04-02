package com.bankcore.accounts.dto.requests;

import com.bankcore.accounts.utils.enums.TransactionType;
import com.bankcore.accounts.utils.validators.query.TransactionQueryValidator;
import com.bankcore.accounts.utils.validators.query.ValidTransactionQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code TransactionQueryParams} is a Data Transfer Object (DTO) that encapsulates query parameters
 * for retrieving transactions.
 *
 * <p>This class is annotated with {@link ValidTransactionQuery}, which ensures that date ranges and
 * transaction types are valid according to business rules.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Provide pagination parameters ({@code page}, {@code size}).
 *   <li>Hold optional filtering parameters ({@code fromDate}, {@code toDate}, {@code type}).
 *   <li>Enforce validation constraints such as minimum/maximum values and ISO-8601 date formats.
 * </ul>
 *
 * <p>Validation Rules:
 *
 * <ul>
 *   <li>{@code page} must be greater than or equal to 1.
 *   <li>{@code size} must be between 1 and 50.
 *   <li>{@code fromDate} and {@code toDate} must be ISO-8601 formatted strings.
 *   <li>{@code fromDate} cannot be after {@code toDate}.
 *   <li>{@code type} must correspond to a valid {@link TransactionType}.
 * </ul>
 *
 * <p>This object is typically used in service or repository layers to filter transaction queries
 * based on user-provided parameters.
 *
 * @author BankcoreTeam
 * @author Sebastian Orjuela
 * @version 0.1.0
 * @see ValidTransactionQuery
 * @see TransactionQueryValidator
 */
@Schema(description = "Query parameters for filtering and pagination of transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidTransactionQuery
public class TransactionQueryParams {

  /** Default values */
  public static final int DEFAULT_PAGE = 1;

  public static final int DEFAULT_SIZE = 20;

  /** The page number for pagination. Must be greater than or equal to 0. */
  @Schema(
      description = "Page number (starting from 1)",
      example = "1",
      minimum = "1",
      defaultValue = "1")
  @Min(value = 1, message = "Page must be greater than or equal to 1")
  @Builder.Default
  private Integer page = DEFAULT_PAGE;

  /** The page size for pagination. Must be between 1 and 50. */
  @Schema(
      description = "Number of records per page",
      example = "20",
      minimum = "1",
      maximum = "50",
      defaultValue = "20")
  @Min(value = 1, message = "Size must be greater than or equal to 1")
  @Max(value = 50, message = "Size must be less than or equal to 50")
  @Builder.Default
  private Integer size = DEFAULT_SIZE;

  /** The start date of the transaction query. Must be in ISO-8601 format. */
  @Schema(
      description = "Start date for filtering transactions (ISO-8601 format)",
      example = "2025-01-01T00:00:00Z",
      format = "date-time")
  private String fromDate;

  /** The end date of the transaction query. Must be in ISO-8601 format. */
  @Schema(
      description = "End date for filtering transactions (ISO-8601 format)",
      example = "2025-01-31T23:59:59Z",
      format = "date-time")
  private String toDate;

  /** The type of transaction to filter. Must correspond to a valid {@link TransactionType}. */
  @Schema(
      description = "Transaction type filter",
      example = "TRANSFER",
      allowableValues = {"DEPOSIT", "WITHDRAWAL", "TRANSFER_IN", "TRANSFER_OUT", "FEE"})
  private String type;

  /**
   * Returns the requested page number or a default value.
   *
   * <p>If the query parameter {@code page} is passed as an empty string (""), Spring's DataBinder
   * will set it to {@code null}. In that case, this method ensures a default value of {@code
   * #DEFAULT_PAGE} is returned.
   *
   * @return the page number, or {@code #DEFAULT_PAGE} if {@code page} is null
   */
  public Integer getPage() {
    return page != null ? page : DEFAULT_PAGE;
  }

  /**
   * Returns the requested page size or a default value.
   *
   * <p>If the query parameter {@code size} is passed as an empty string (""), Spring's DataBinder
   * will set it to {@code null}. In that case, this method ensures a default value of {@code
   * #DEFAULT_SIZE} is returned.
   *
   * @return the page size, or {@code #DEFAULT_SIZE} if {@code size} is null
   */
  public Integer getSize() {
    return size != null ? size : DEFAULT_SIZE;
  }
}

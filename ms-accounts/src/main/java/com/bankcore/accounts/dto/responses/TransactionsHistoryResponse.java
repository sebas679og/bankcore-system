package com.bankcore.accounts.dto.responses;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * {@code TransactionsHistoryResponse} is an immutable Data Transfer Object (DTO) that represents a
 * paginated list of transaction history responses.
 *
 * <p>This class is typically used as an API response model to provide clients with structured
 * transaction history data, including pagination metadata such as page number, page size, total
 * elements, and total pages.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Expose a list of {@link TransactionHistoryResponse} objects.
 *   <li>Provide pagination details ({@code page}, {@code size}).
 *   <li>Expose metadata about the total number of elements and pages.
 *   <li>Ensure immutability through Lombok's {@link lombok.Value} annotation.
 *   <li>Support builder pattern via {@link lombok.Builder} for easy instantiation.
 * </ul>
 *
 * <p>This object is returned by APIs to provide clients with a paginated transaction history,
 * ensuring consistency and clarity in response models.
 *
 * @author BankcoreTeam
 * @author Sebastian Orjuela
 * @version 0.2.0
 * @see TransactionHistoryResponse
 */
@Value
@Getter
@Builder
@Jacksonized
public class TransactionsHistoryResponse {

  /** The list of transaction history responses for the current page. */
  List<TransactionHistoryResponse> content;

  /** The current page number in the paginated response. */
  int page;

  /** The size of the page (number of items per page). */
  int size;

  /** The total number of elements across all pages. */
  Long totalElements;

  /** The total number of pages available. */
  int totalPages;
}

package com.bankcore.accounts.services.processors;

import com.bankcore.accounts.dto.requests.TransactionQueryParams;
import com.bankcore.accounts.dto.responses.TransactionHistoryResponse;
import com.bankcore.accounts.dto.responses.TransactionsHistoryResponse;
import com.bankcore.accounts.exceptions.AccountNotFoundException;
import com.bankcore.accounts.models.TransactionEntity;
import com.bankcore.accounts.repositories.AccountRepository;
import com.bankcore.accounts.repositories.TransactionRepository;
import com.bankcore.accounts.repositories.TransactionSpecification;
import com.bankcore.accounts.utils.enums.TransactionType;
import com.bankcore.accounts.utils.mappers.TransactionMapper;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code TransactionHistoryProcessor} is a Spring service component responsible for processing
 * transaction history queries.
 *
 * <p>This service orchestrates the retrieval of transaction data from the repository, applies
 * filters based on query parameters, and maps entities into API response DTOs.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Parse and validate query parameters such as date range and type.
 *   <li>Delegate data retrieval to {@link TransactionRepository}.
 *   <li>Apply pagination using {@link org.springframework.data.domain.Pageable}.
 *   <li>Map {@link TransactionEntity} objects into {@link TransactionHistoryResponse} DTOs via
 *       {@link TransactionMapper}.
 *   <li>Build a {@link TransactionsHistoryResponse} containing content and pagination metadata.
 * </ul>
 *
 * <p>This service ensures that transaction history queries are handled consistently, with proper
 * validation, filtering, and mapping.
 *
 * @author BankcoreTeam
 * @author Sebastian Orjuela
 * @version 0.1.0
 * @see TransactionRepository
 * @see AccountRepository
 * @see TransactionMapper
 * @see TransactionsHistoryResponse
 * @see TransactionHistoryResponse
 * @see TransactionQueryParams
 */
@Service
@RequiredArgsConstructor
public class TransactionHistoryProcessor {

  private final TransactionRepository repository;
  private final TransactionMapper transactionMapper;
  private final AccountRepository accountRepository;

  /**
   * Retrieves a paginated transaction history for the given account, applying optional filters such
   * as type and date range.
   *
   * @param accountId the unique identifier of the account
   * @param customerId the unique identifier of the customer
   * @param params the query parameters including pagination, type, and date filters
   * @return a {@link TransactionsHistoryResponse} containing transaction history and pagination
   *     metadata
   * @throws AccountNotFoundException if the account does not exist
   */
  @Transactional(readOnly = true)
  public TransactionsHistoryResponse getTransactions(
      UUID accountId, UUID customerId, TransactionQueryParams params) {

    if (params == null) {
      throw new IllegalArgumentException("TransactionQueryParams must not be null");
    }

    if (!accountRepository.existsByIdAndCustomerId(accountId, customerId)) {
      throw new AccountNotFoundException();
    }

    Instant from = params.getFromDate() != null ? Instant.parse(params.getFromDate()) : null;
    Instant to = params.getToDate() != null ? Instant.parse(params.getToDate()) : null;

    TransactionType type = null;
    if (params.getType() != null) {
      type = TransactionType.valueOf(params.getType().toUpperCase(Locale.ROOT));
    }

    Page<TransactionEntity> pageResult =
        repository.findAll(
            TransactionSpecification.withFilters(accountId, type, from, to),
            TransactionSpecification.toPageable(params.getPage() - 1, params.getSize()));

    List<TransactionHistoryResponse> content =
        pageResult.stream().map(transactionMapper::toTransactionHistory).toList();

    return TransactionsHistoryResponse.builder()
        .content(content)
        .page(params.getPage())
        .size(params.getSize())
        .totalElements(pageResult.getTotalElements())
        .totalPages(pageResult.getTotalPages())
        .build();
  }
}

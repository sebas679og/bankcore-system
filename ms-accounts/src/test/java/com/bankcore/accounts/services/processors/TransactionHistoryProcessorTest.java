package com.bankcore.accounts.services.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.bankcore.accounts.dto.requests.TransactionQueryParams;
import com.bankcore.accounts.dto.responses.TransactionHistoryResponse;
import com.bankcore.accounts.dto.responses.TransactionsHistoryResponse;
import com.bankcore.accounts.exceptions.AccountNotFoundException;
import com.bankcore.accounts.models.TransactionEntity;
import com.bankcore.accounts.repositories.AccountRepository;
import com.bankcore.accounts.repositories.TransactionRepository;
import com.bankcore.accounts.utils.mappers.TransactionMapper;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionHistoryProcessor")
public class TransactionHistoryProcessorTest {

  @Mock TransactionRepository repository;

  @Mock TransactionMapper transactionMapper;

  @Mock AccountRepository accountRepository;

  @InjectMocks TransactionHistoryProcessor processor;

  private UUID accountId;
  private UUID customerId;

  @BeforeEach
  void setUp() {
    accountId = UUID.randomUUID();
    customerId = UUID.randomUUID();
  }

  // ── helpers ───────────────────────────────────────────────────────────────
  private TransactionQueryParams baseParams(int page, int size) {
    TransactionQueryParams p = new TransactionQueryParams();
    p.setPage(page);
    p.setSize(size);
    return p;
  }

  private Page<TransactionEntity> emptyPage(Pageable pageable) {
    return new PageImpl<>(Collections.emptyList(), pageable, 0);
  }

  private Page<TransactionEntity> pageOf(List<TransactionEntity> entities, Pageable pageable) {
    return new PageImpl<>(entities, pageable, entities.size());
  }

  // ─── AccountNotFoundException ─────────────────────────────────────────────

  @Nested
  @DisplayName("when account does not exist")
  class AccountNotFound {

    @Test
    @DisplayName("throws AccountNotFoundException")
    void shouldThrowWhenAccountNotFound() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(false);

      assertThatThrownBy(() -> processor.getTransactions(accountId, customerId, baseParams(1, 10)))
          .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("never queries the transaction repository")
    void shouldNotQueryRepositoryWhenAccountMissing() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(false);

      try {
        processor.getTransactions(accountId, customerId, baseParams(1, 10));
      } catch (AccountNotFoundException ignored) {
      }

      verifyNoInteractions(repository);
    }
  }

  // ─── Pagination ───────────────────────────────────────────────────────────

  @Nested
  @DisplayName("pagination")
  class Pagination {

    @Test
    @DisplayName("converts 1-based page param to 0-based Pageable")
    void shouldConvertPageToZeroBased() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);
      when(repository.findAll(any(Specification.class), any(Pageable.class)))
          .thenAnswer(inv -> emptyPage(inv.getArgument(1)));

      processor.getTransactions(accountId, customerId, baseParams(1, 10));

      ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
      verify(repository).findAll(any(Specification.class), captor.capture());
      assertThat(captor.getValue().getPageNumber()).isZero();
    }

    @Test
    @DisplayName("passes correct page size to Pageable")
    void shouldPassCorrectPageSize() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);
      when(repository.findAll(any(Specification.class), any(Pageable.class)))
          .thenAnswer(inv -> emptyPage(inv.getArgument(1)));

      processor.getTransactions(accountId, customerId, baseParams(1, 15));

      ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
      verify(repository).findAll(any(Specification.class), captor.capture());
      assertThat(captor.getValue().getPageSize()).isEqualTo(15);
    }

    @Test
    @DisplayName("sorts by createdAt descending")
    void shouldSortByCreatedAtDescending() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);
      when(repository.findAll(any(Specification.class), any(Pageable.class)))
          .thenAnswer(inv -> emptyPage(inv.getArgument(1)));

      processor.getTransactions(accountId, customerId, baseParams(1, 10));

      ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
      verify(repository).findAll(any(Specification.class), captor.capture());

      Sort.Order order = captor.getValue().getSort().getOrderFor("createdAt");
      assertThat(order).isNotNull();
      assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("returns correct page and size metadata in response")
    void shouldReturnPageAndSizeMetadata() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);
      when(repository.findAll(any(Specification.class), any(Pageable.class)))
          .thenAnswer(inv -> emptyPage(inv.getArgument(1)));

      TransactionsHistoryResponse response =
          processor.getTransactions(accountId, customerId, baseParams(3, 20));

      assertThat(response.getPage()).isEqualTo(3);
      assertThat(response.getSize()).isEqualTo(20);
    }
  }

  // ─── Optional filters ─────────────────────────────────────────────────────

  @Nested
  @DisplayName("optional filters")
  class OptionalFilters {

    @Test
    @DisplayName("parses fromDate as Instant when provided")
    void shouldParseFromDate() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);

      TransactionQueryParams params = baseParams(1, 10);
      params.setFromDate("2024-01-01T00:00:00Z");

      when(repository.findAll(any(Specification.class), any(Pageable.class)))
          .thenAnswer(inv -> emptyPage(inv.getArgument(1)));

      // No exception means Instant.parse succeeded
      assertThat(processor.getTransactions(accountId, customerId, params)).isNotNull();
    }

    @Test
    @DisplayName("parses toDate as Instant when provided")
    void shouldParseToDate() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);

      TransactionQueryParams params = baseParams(1, 10);
      params.setToDate("2024-12-31T23:59:59Z");

      when(repository.findAll(any(Specification.class), any(Pageable.class)))
          .thenAnswer(inv -> emptyPage(inv.getArgument(1)));

      assertThat(processor.getTransactions(accountId, customerId, params)).isNotNull();
    }

    @Test
    @DisplayName("accepts null fromDate without error")
    void shouldHandleNullFromDate() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);
      when(repository.findAll(any(Specification.class), any(Pageable.class)))
          .thenAnswer(inv -> emptyPage(inv.getArgument(1)));

      TransactionQueryParams params = baseParams(1, 10);
      params.setFromDate(null);

      assertThat(processor.getTransactions(accountId, customerId, params)).isNotNull();
    }

    @Test
    @DisplayName("accepts null toDate without error")
    void shouldHandleNullToDate() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);
      when(repository.findAll(any(Specification.class), any(Pageable.class)))
          .thenAnswer(inv -> emptyPage(inv.getArgument(1)));

      TransactionQueryParams params = baseParams(1, 10);
      params.setToDate(null);

      assertThat(processor.getTransactions(accountId, customerId, params)).isNotNull();
    }

    @Test
    @DisplayName("parses transaction type to uppercase enum")
    void shouldParseTypeToUppercase() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);
      when(repository.findAll(any(Specification.class), any(Pageable.class)))
          .thenAnswer(inv -> emptyPage(inv.getArgument(1)));

      TransactionQueryParams params = baseParams(1, 10);
      params.setType("deposit"); // lowercase — should be normalised

      // Should not throw IllegalArgumentException
      assertThat(processor.getTransactions(accountId, customerId, params)).isNotNull();
    }

    @Test
    @DisplayName("accepts null type without error")
    void shouldHandleNullType() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);
      when(repository.findAll(any(Specification.class), any(Pageable.class)))
          .thenAnswer(inv -> emptyPage(inv.getArgument(1)));

      TransactionQueryParams params = baseParams(1, 10);
      params.setType(null);

      assertThat(processor.getTransactions(accountId, customerId, params)).isNotNull();
    }

    @Test
    @DisplayName("throws IllegalArgumentException for unknown transaction type")
    void shouldThrowForUnknownType() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);

      TransactionQueryParams params = baseParams(1, 10);
      params.setType("INVALID_TYPE");

      assertThatThrownBy(() -> processor.getTransactions(accountId, customerId, params))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  // ─── Response mapping ─────────────────────────────────────────────────────

  @Nested
  @DisplayName("response mapping")
  class ResponseMapping {

    @Test
    @DisplayName("maps each entity via transactionMapper")
    void shouldMapAllEntitiesWithMapper() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);

      TransactionEntity e1 = TransactionEntity.builder().build();
      TransactionEntity e2 = TransactionEntity.builder().build();
      TransactionHistoryResponse r1 = TransactionHistoryResponse.builder().build();
      TransactionHistoryResponse r2 = TransactionHistoryResponse.builder().build();

      when(transactionMapper.toTransactionHistory(e1)).thenReturn(r1);
      when(transactionMapper.toTransactionHistory(e2)).thenReturn(r2);

      when(repository.findAll(any(Specification.class), any(Pageable.class)))
          .thenAnswer(inv -> pageOf(List.of(e1, e2), inv.getArgument(1)));

      TransactionsHistoryResponse response =
          processor.getTransactions(accountId, customerId, baseParams(1, 10));

      assertThat(response.getContent()).containsExactly(r1, r2);
      verify(transactionMapper).toTransactionHistory(e1);
      verify(transactionMapper).toTransactionHistory(e2);
    }

    @Test
    @DisplayName("returns empty content list when page has no results")
    void shouldReturnEmptyContentWhenNoResults() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);
      when(repository.findAll(any(Specification.class), any(Pageable.class)))
          .thenAnswer(inv -> emptyPage(inv.getArgument(1)));

      TransactionsHistoryResponse response =
          processor.getTransactions(accountId, customerId, baseParams(1, 10));

      assertThat(response.getContent()).isEmpty();
      verifyNoInteractions(transactionMapper);
    }

    @Test
    @DisplayName("sets totalElements from page result")
    void shouldSetTotalElements() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);

      TransactionEntity e1 = TransactionEntity.builder().build();
      when(transactionMapper.toTransactionHistory(any()))
          .thenReturn(TransactionHistoryResponse.builder().build());
      when(repository.findAll(any(Specification.class), any(Pageable.class)))
          .thenAnswer(inv -> new PageImpl<>(List.of(e1), inv.getArgument(1), 42L));

      TransactionsHistoryResponse response =
          processor.getTransactions(accountId, customerId, baseParams(1, 10));

      assertThat(response.getTotalElements()).isEqualTo(42L);
    }

    @Test
    @DisplayName("sets totalPages from page result")
    void shouldSetTotalPages() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);
      when(repository.findAll(any(Specification.class), any(Pageable.class)))
          .thenAnswer(inv -> new PageImpl<>(Collections.emptyList(), inv.getArgument(1), 0L));

      TransactionsHistoryResponse response =
          processor.getTransactions(accountId, customerId, baseParams(1, 10));

      assertThat(response.getTotalPages()).isZero();
    }

    @Test
    @DisplayName("response content size matches entity list size")
    void shouldMatchContentSizeToEntityList() {
      when(accountRepository.existsByIdAndCustomerId(accountId, customerId)).thenReturn(true);

      List<TransactionEntity> entities =
          List.of(
              TransactionEntity.builder().build(),
              TransactionEntity.builder().build(),
              TransactionEntity.builder().build());
      when(transactionMapper.toTransactionHistory(any()))
          .thenReturn(TransactionHistoryResponse.builder().build());
      when(repository.findAll(any(Specification.class), any(Pageable.class)))
          .thenAnswer(inv -> pageOf(entities, inv.getArgument(1)));

      TransactionsHistoryResponse response =
          processor.getTransactions(accountId, customerId, baseParams(1, 10));

      assertThat(response.getContent()).hasSize(3);
    }
  }
}

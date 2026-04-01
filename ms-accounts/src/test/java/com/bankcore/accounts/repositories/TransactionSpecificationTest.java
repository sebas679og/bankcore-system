package com.bankcore.accounts.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bankcore.accounts.models.TransactionEntity;
import com.bankcore.accounts.utils.enums.TransactionType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/** Unit tests for {@link TransactionSpecificationTest}. */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionSpecification")
public class TransactionSpecificationTest {

  @Nested
  @DisplayName("withFilters()")
  class WithFilters {

    @Mock Root<TransactionEntity> root;
    @Mock CriteriaQuery<?> query;
    @Mock CriteriaBuilder cb;
    @Mock Path<Object> accountPath;
    @Mock Path<Object> accountIdPath;
    @Mock Path<Object> typePath;
    @Mock Path<Instant> createdAtPath;
    @Mock Predicate accountPredicate;
    @Mock Predicate typePredicate;
    @Mock Predicate fromDatePredicate;
    @Mock Predicate toDatePredicate;
    @Mock Predicate combinedPredicate;

    private final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
      // root.get("account").get("id")
      when(root.get("account")).thenReturn(accountPath);
      when(accountPath.get("id")).thenReturn(accountIdPath);
      when(cb.equal(accountIdPath, accountId)).thenReturn(accountPredicate);

      // root.get("type") and root.get("createdAt")
      lenient().when(root.get("type")).thenReturn(typePath);
      lenient().when(root.get("createdAt")).thenReturn((Path) createdAtPath);
    }

    // ── helper to capture the Predicate[] passed to cb.and() ─────────────
    private Predicate[] captureAndPredicates() {
      ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
      verify(cb).and(captor.capture());
      return captor.getValue();
    }

    @Test
    @DisplayName("always adds accountId predicate")
    void shouldAlwaysFilterByAccountId() {
      when(cb.and(any(Predicate[].class))).thenReturn(combinedPredicate);

      Specification<TransactionEntity> spec =
          TransactionSpecification.withFilters(accountId, null, null, null);

      spec.toPredicate(root, query, cb);

      verify(cb).equal(accountIdPath, accountId);
      Predicate[] predicates = captureAndPredicates();
      assertThat(predicates).containsExactly(accountPredicate);
    }

    @Test
    @DisplayName("adds type predicate when type is not null")
    void shouldFilterByTypeWhenProvided() {
      TransactionType type = TransactionType.DEPOSIT;
      when(cb.equal(typePath, type)).thenReturn(typePredicate);
      when(cb.and(any(Predicate[].class))).thenReturn(combinedPredicate);

      Specification<TransactionEntity> spec =
          TransactionSpecification.withFilters(accountId, type, null, null);

      spec.toPredicate(root, query, cb);

      verify(cb).equal(typePath, type);
      Predicate[] predicates = captureAndPredicates();
      assertThat(predicates).containsExactly(accountPredicate, typePredicate);
    }

    @Test
    @DisplayName("does not add type predicate when type is null")
    void shouldNotFilterByTypeWhenNull() {
      when(cb.and(any(Predicate[].class))).thenReturn(combinedPredicate);

      Specification<TransactionEntity> spec =
          TransactionSpecification.withFilters(accountId, null, null, null);

      spec.toPredicate(root, query, cb);

      verify(cb, never()).equal(eq(typePath), any(TransactionType.class));
      Predicate[] predicates = captureAndPredicates();
      assertThat(predicates).hasSize(1);
    }

    @Test
    @DisplayName("adds fromDate predicate (>=) when fromDate is not null")
    void shouldFilterByFromDateWhenProvided() {
      Instant fromDate = Instant.parse("2024-01-01T00:00:00Z");
      when(cb.greaterThanOrEqualTo(createdAtPath, fromDate)).thenReturn(fromDatePredicate);
      when(cb.and(any(Predicate[].class))).thenReturn(combinedPredicate);

      Specification<TransactionEntity> spec =
          TransactionSpecification.withFilters(accountId, null, fromDate, null);

      spec.toPredicate(root, query, cb);

      verify(cb).greaterThanOrEqualTo(createdAtPath, fromDate);
      Predicate[] predicates = captureAndPredicates();
      assertThat(predicates).containsExactly(accountPredicate, fromDatePredicate);
    }

    @Test
    @DisplayName("does not add fromDate predicate when fromDate is null")
    void shouldNotFilterByFromDateWhenNull() {
      when(cb.and(any(Predicate[].class))).thenReturn(combinedPredicate);

      Specification<TransactionEntity> spec =
          TransactionSpecification.withFilters(accountId, null, null, null);

      spec.toPredicate(root, query, cb);

      verify(cb, never()).greaterThanOrEqualTo(any(), any(Instant.class));
    }

    @Test
    @DisplayName("adds toDate predicate (<=) when toDate is not null")
    void shouldFilterByToDateWhenProvided() {
      Instant toDate = Instant.parse("2024-12-31T23:59:59Z");
      when(cb.lessThanOrEqualTo(createdAtPath, toDate)).thenReturn(toDatePredicate);
      when(cb.and(any(Predicate[].class))).thenReturn(combinedPredicate);

      Specification<TransactionEntity> spec =
          TransactionSpecification.withFilters(accountId, null, null, toDate);

      spec.toPredicate(root, query, cb);

      verify(cb).lessThanOrEqualTo(createdAtPath, toDate);
      Predicate[] predicates = captureAndPredicates();
      assertThat(predicates).containsExactly(accountPredicate, toDatePredicate);
    }

    @Test
    @DisplayName("does not add toDate predicate when toDate is null")
    void shouldNotFilterByToDateWhenNull() {
      when(cb.and(any(Predicate[].class))).thenReturn(combinedPredicate);

      Specification<TransactionEntity> spec =
          TransactionSpecification.withFilters(accountId, null, null, null);

      spec.toPredicate(root, query, cb);

      verify(cb, never()).lessThanOrEqualTo(any(), any(Instant.class));
    }

    @Test
    @DisplayName("adds all predicates when all parameters are provided")
    void shouldAddAllPredicatesWhenAllParamsProvided() {
      TransactionType type = TransactionType.WITHDRAWAL;
      Instant fromDate = Instant.parse("2024-01-01T00:00:00Z");
      Instant toDate = Instant.parse("2024-12-31T23:59:59Z");

      when(cb.equal(typePath, type)).thenReturn(typePredicate);
      when(cb.greaterThanOrEqualTo(createdAtPath, fromDate)).thenReturn(fromDatePredicate);
      when(cb.lessThanOrEqualTo(createdAtPath, toDate)).thenReturn(toDatePredicate);
      when(cb.and(any(Predicate[].class))).thenReturn(combinedPredicate);

      Specification<TransactionEntity> spec =
          TransactionSpecification.withFilters(accountId, type, fromDate, toDate);

      spec.toPredicate(root, query, cb);

      Predicate[] predicates = captureAndPredicates();
      assertThat(predicates)
          .hasSize(4)
          .containsExactly(accountPredicate, typePredicate, fromDatePredicate, toDatePredicate);
    }

    @Test
    @DisplayName("returns the combined Predicate from cb.and()")
    void shouldReturnCombinedPredicate() {
      when(cb.and(any(Predicate[].class))).thenReturn(combinedPredicate);

      Specification<TransactionEntity> spec =
          TransactionSpecification.withFilters(accountId, null, null, null);

      Predicate result = spec.toPredicate(root, query, cb);

      assertThat(result).isSameAs(combinedPredicate);
    }

    @Test
    @DisplayName("handles fromDate and toDate without type")
    void shouldFilterByDateRangeWithoutType() {
      Instant fromDate = Instant.parse("2024-06-01T00:00:00Z");
      Instant toDate = Instant.parse("2024-06-30T23:59:59Z");

      when(cb.greaterThanOrEqualTo(createdAtPath, fromDate)).thenReturn(fromDatePredicate);
      when(cb.lessThanOrEqualTo(createdAtPath, toDate)).thenReturn(toDatePredicate);
      when(cb.and(any(Predicate[].class))).thenReturn(combinedPredicate);

      Specification<TransactionEntity> spec =
          TransactionSpecification.withFilters(accountId, null, fromDate, toDate);

      spec.toPredicate(root, query, cb);

      verify(cb, never()).equal(eq(typePath), any());
      Predicate[] predicates = captureAndPredicates();
      assertThat(predicates)
          .hasSize(3)
          .containsExactly(accountPredicate, fromDatePredicate, toDatePredicate);
    }
  }

  // ─── toPageable ───────────────────────────────────────────────────────────

  @Nested
  @DisplayName("toPageable()")
  class ToPageable {

    @Test
    @DisplayName("returns Pageable with the given page number")
    void shouldReturnCorrectPageNumber() {
      Pageable pageable = TransactionSpecification.toPageable(2, 10);
      assertThat(pageable.getPageNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("returns Pageable with the given page size")
    void shouldReturnCorrectPageSize() {
      Pageable pageable = TransactionSpecification.toPageable(0, 25);
      assertThat(pageable.getPageSize()).isEqualTo(25);
    }

    @Test
    @DisplayName("sorts by createdAt in descending order")
    void shouldSortByCreatedAtDescending() {
      Pageable pageable = TransactionSpecification.toPageable(0, 10);

      Sort.Order order = pageable.getSort().getOrderFor("createdAt");

      assertThat(order).isNotNull();
      assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("sort contains only createdAt field")
    void shouldSortOnlyByCreatedAt() {
      Pageable pageable = TransactionSpecification.toPageable(0, 10);
      assertThat(pageable.getSort()).hasSize(1);
    }

    @Test
    @DisplayName("returns first page when page=0")
    void shouldReturnFirstPage() {
      Pageable pageable = TransactionSpecification.toPageable(0, 5);
      assertThat(pageable.getPageNumber()).isZero();
    }

    @Test
    @DisplayName("is equivalent to the expected PageRequest")
    void shouldMatchExpectedPageRequest() {
      Pageable expected = PageRequest.of(3, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
      Pageable actual = TransactionSpecification.toPageable(3, 20);
      assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("returns different Pageable instances for different pages")
    void shouldReturnDifferentPageablesForDifferentPages() {
      Pageable page0 = TransactionSpecification.toPageable(0, 10);
      Pageable page1 = TransactionSpecification.toPageable(1, 10);
      assertThat(page0).isNotEqualTo(page1);
    }
  }
}

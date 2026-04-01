package com.bankcore.accounts.repositories;

import com.bankcore.accounts.models.TransactionEntity;
import com.bankcore.accounts.utils.enums.TransactionType;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 * {@code TransactionSpecification} provides dynamic query specifications for filtering {@link
 * TransactionEntity} objects using JPA Criteria API.
 *
 * <p>This class is typically used in repositories to build flexible queries based on optional
 * filters such as account ID, transaction type, and date range.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Construct reusable {@link Specification} objects for transaction queries.
 *   <li>Support optional filters without requiring multiple query methods.
 *   <li>Encapsulate query logic in a clean and maintainable way.
 * </ul>
 *
 * @author Bankcore Team
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
public class TransactionSpecification {

  /**
   * Builds a {@link Specification} for filtering transactions by account ID, transaction type, and
   * date range.
   *
   * <p>Filter rules:
   *
   * <ul>
   *   <li>Always filters by {@code accountId}.
   *   <li>If {@code type} is not null, filters by transaction type.
   *   <li>If {@code fromDate} is not null, includes transactions created on or after this date.
   *   <li>If {@code toDate} is not null, includes transactions created on or before this date.
   * </ul>
   *
   * @param accountId the unique identifier of the account
   * @param type optional transaction type filter (nullable)
   * @param fromDate optional start date filter (nullable)
   * @param toDate optional end date filter (nullable)
   * @return a {@link Specification} that applies the given filters
   */
  public static Specification<TransactionEntity> withFilters(
      UUID accountId, TransactionType type, Instant fromDate, Instant toDate) {

    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      predicates.add(cb.equal(root.get("account").get("id"), accountId));

      if (type != null) {
        predicates.add(cb.equal(root.get("type"), type));
      }
      if (fromDate != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
      }
      if (toDate != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  /**
   * Builds a {@link Pageable} object with the given page number and size, applying a default sort
   * order by {@code createdAt} in descending direction.
   *
   * <p>This utility method ensures that transaction queries are consistently paginated and ordered
   * by the most recent entries first.
   *
   * @param page the page number (zero-based index)
   * @param size the number of elements per page
   * @return a {@link Pageable} object with descending sort by {@code createdAt}
   */
  public static Pageable toPageable(int page, int size) {
    return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
  }
}

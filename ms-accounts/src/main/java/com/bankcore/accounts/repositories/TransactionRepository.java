package com.bankcore.accounts.repositories;

import com.bankcore.accounts.models.TransactionEntity;
import com.bankcore.accounts.utils.enums.TransactionStatus;
import com.bankcore.accounts.utils.enums.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for managing {@link TransactionEntity} persistence operations.
 *
 * <p>Extends {@link JpaRepository} to provide standard CRUD operations and exposes custom query
 * methods for transaction-specific lookups.
 *
 * @author BankCore Team
 * @author Cristian Ortiz
 * @author Sebastian Orjuela
 * @version 0.3.0
 */
public interface TransactionRepository
    extends JpaRepository<TransactionEntity, UUID>, JpaSpecificationExecutor<TransactionEntity> {

  /**
   * Retrieves the most recent transaction associated with a given account.
   *
   * <p>Results are ordered by {@code createdAt} in descending order, returning only the latest
   * record.
   *
   * @param accountId the UUID of the account whose latest transaction is requested
   * @return an {@link Optional} containing the latest {@link TransactionEntity}, or empty if no
   *     transactions exist for the given account
   */
  Optional<TransactionEntity> findTopByAccount_IdOrderByCreatedAtDesc(UUID accountId);

  /**
   * Calculates the total amount of completed withdrawals for a specific account since a given time.
   *
   * <p>This method uses a JPQL query to sum the transaction amounts where the type, status, and
   * creation timestamp match the provided criteria. {@code COALESCE} is used to return a default
   * value of {@code 0.0} if no matching transactions are found.
   *
   * @param accountId the unique identifier of the account
   * @param type the {@link TransactionType} (e.g., WITHDRAWAL)
   * @param status the {@link TransactionStatus} (e.g., COMPLETED)
   * @param startOfDay the inclusive start time for the calculation (typically the start of the
   *     current UTC day)
   * @return the total sum of withdrawals as a {@link BigDecimal}
   */
  @Query(
      "SELECT COALESCE(SUM(t.amount), 0.0) FROM TransactionEntity t "
          + "WHERE t.account.id = :accountId AND t.type = :type "
          + "AND t.status = :status AND t.createdAt >= :startOfDay")
  BigDecimal calculateDailyWithdrawalTotal(
      @Param("accountId") UUID accountId,
      @Param("type") TransactionType type,
      @Param("status") TransactionStatus status,
      @Param("startOfDay") Instant startOfDay);
}

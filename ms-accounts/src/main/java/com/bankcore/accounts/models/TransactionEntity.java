package com.bankcore.accounts.models;

import com.bankcore.accounts.utils.enums.TransactionStatus;
import com.bankcore.accounts.utils.enums.TransactionType;
import com.github.f4b6a3.uuid.UuidCreator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * Represents a financial transaction associated with an {@link AccountEntity}.
 *
 * <p>This entity is immutable and mapped to the {@code transactions} table. It stores details such
 * as transaction type, amount, balance after the transaction, counterparty information, and
 * metadata like reference number and creation timestamp.
 *
 * <p><b>Constraints and Indexes:</b>
 *
 * <ul>
 *   <li>Unique constraint on {@code referenceNumber} to ensure transaction uniqueness.
 *   <li>Indexes to optimize queries by account, type, status, and creation date.
 * </ul>
 *
 * <p><b>Lifecycle:</b>
 *
 * <ul>
 *   <li>On persist, generates a time-ordered UUID (UUID v7) if not provided.
 *   <li>Automatically sets {@code createdAt} and {@code referenceNumber} if missing.
 * </ul>
 *
 * <p><b>Immutability:</b> All fields are non-updatable after persistence, ensuring transaction
 * records remain immutable and auditable.
 *
 * @author Banckore Team
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Immutable
@Entity
@Table(
    name = "transactions",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_reference_number", columnNames = "referenceNumber")
    },
    indexes = {
      @Index(name = "idx_transaction_account", columnList = "account_id"),
      @Index(name = "idx_transaction_account_created", columnList = "account_id, created_at DESC"),
      @Index(
          name = "idx_transaction_account_type_status_created",
          columnList = "account_id, type, status, created_at"),
      @Index(name = "idx_account_type_created_at", columnList = "account_id, type, created_at DESC")
    })
public class TransactionEntity {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false, updatable = false)
  private AccountEntity account;

  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private TransactionType type;

  @Column(nullable = false, precision = 19, scale = 2, updatable = false)
  private BigDecimal amount;

  @Column(nullable = false, precision = 19, scale = 2, updatable = false)
  private BigDecimal balanceAfter;

  @Column(updatable = false)
  private String description;

  @Column(updatable = false)
  private String counterpartyAccountNumber;

  @Column(updatable = false)
  private String counterpartyName;

  @Column(nullable = false, updatable = false)
  private String referenceNumber;

  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private TransactionStatus status;

  @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant createdAt;

  /**
   * Generates a unique reference number based on the transaction UUID and a random component. This
   * method is invoked during entity creation if no reference number is set.
   */
  protected void generateReferenceNumber() {
    if (this.id != null) {
      String uuidPart = this.id.toString().replace("-", "").substring(0, 16).toUpperCase();
      String randomPart =
          Long.toHexString(ThreadLocalRandom.current().nextLong(0xFFFFFFFFFFFFL)).toUpperCase();
      this.referenceNumber = String.join("", "TXN", uuidPart, randomPart);
    }
  }

  /**
   * Lifecycle callback executed before persisting the entity.
   *
   * <ul>
   *   <li>Generates a UUID v7 if {@code id} is null.
   *   <li>Sets {@code createdAt} to the current timestamp if null.
   *   <li>Generates a {@code referenceNumber} if not provided.
   * </ul>
   */
  @PrePersist
  protected void onCreate() {
    if (this.id == null) {
      this.id = UuidCreator.getTimeOrderedEpoch(); // UUID v7
    }

    if (this.createdAt == null) {
      this.createdAt = Instant.now();
    }

    if (this.referenceNumber == null) {
      generateReferenceNumber();
    }
  }
}

package com.bankcore.accounts.models;

import com.bankcore.accounts.utils.enums.TransferStatus;
import com.bankcore.accounts.utils.uuidConfig.UUIDv7;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * Represents a transfer record between accounts in the system.
 *
 * <p>This entity models the lifecycle of a financial transfer, including source account,
 * destination account number, amount, fees, status, and creation timestamp. It is immutable once
 * persisted, ensuring transactional integrity and auditability.
 *
 * <h2>Database Mapping</h2>
 *
 * <ul>
 *   <li>Mapped to table <b>transfers</b>.
 *   <li>Indexes:
 *       <ul>
 *         <li>{@code idx_transfer_source_account} - Optimizes queries by source account.
 *         <li>{@code idx_transfer_destination_account} - Optimizes queries by destination account
 *             number.
 *         <li>{@code idx_transfer_source_account_created} - Optimizes queries by account and
 *             creation date.
 *       </ul>
 * </ul>
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 0.1.0
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Immutable
@Entity
@Table(
    name = "transfers",
    indexes = {
      @Index(name = "idx_transfer_source_account", columnList = "source_account_id"),
      @Index(name = "idx_transfer_destination_account", columnList = "destination_account_number"),
      @Index(
          name = "idx_transfer_source_account_created",
          columnList = "source_account_id, created_at DESC")
    })
public class TransferEntity {

  @Id
  @GeneratedValue(generator = "uuid7")
  @UUIDv7
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_account_id", nullable = false, updatable = false)
  private AccountEntity account;

  @Column(nullable = false, updatable = false)
  private String destinationAccountNumber;

  @Column(nullable = false, precision = 19, scale = 2, updatable = false)
  private BigDecimal amount;

  @Column(nullable = false, precision = 19, scale = 2, updatable = false)
  private BigDecimal fee;

  @Column(updatable = false)
  private String description;

  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private TransferStatus status;

  @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = Instant.now();
  }
}

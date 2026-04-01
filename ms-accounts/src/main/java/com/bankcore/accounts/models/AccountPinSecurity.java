package com.bankcore.accounts.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing the PIN security configuration for an account.
 *
 * <p>This JPA entity stores information related to PIN validation attempts, including failed
 * attempts, temporary and permanent lock states, and timestamps of the last failed attempt. It is
 * designed to support authentication workflows and account security policies.
 *
 * <h2>Database Mapping:</h2>
 *
 * <ul>
 *   <li>Table: {@code account_pin_security}
 *   <li>Primary Key: {@code account_id}
 *   <li>Index: {@code idx_account_pin_security_account} for efficient lookups by account ID
 * </ul>
 *
 * @author BankCore Team - Sebastian Orjuela
 * @version 0.1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
    name = "account_pin_security",
    indexes = {@Index(name = "idx_account_pin_security_account", columnList = "account_id")})
public class AccountPinSecurity {

  /** Unique identifier of the account associated with this PIN security record. */
  @Id
  @Column(name = "account_id")
  private UUID accountId;

  /**
   * Reference to the {@link AccountEntity} associated with this PIN security record. Uses
   * {@code @MapsId} to share the same primary key as the account.
   */
  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private AccountEntity account;

  /** Number of consecutive failed PIN attempts. Defaults to {@code 0}. */
  @Column @Builder.Default private int failedAttempts = 0;

  /**
   * Timestamp until which the account is temporarily locked due to failed attempts. Defaults to
   * {@code null}.
   */
  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  @Builder.Default
  private Instant temporaryLockUntil = null;

  /** Flag indicating whether the account is permanently locked. Defaults to {@code false}. */
  @Column @Builder.Default private boolean permanentLock = false;

  /** Timestamp of the last failed PIN attempt. Defaults to {@code null}. */
  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  @Builder.Default
  private Instant lastFailedAttemptAt = null;
}

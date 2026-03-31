package com.bankcore.accounts.models;

import com.bankcore.accounts.utils.enums.AccountStatus;
import com.bankcore.accounts.utils.enums.AccountType;
import com.bankcore.accounts.utils.enums.CurrencyCode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

/**
 * Represents a bank account entity in the system. This entity is mapped to the "accounts" table in
 * the database and contains all relevant information about a bank account, including account
 * number, customer ID, account type, currency, balance, alias, status, daily withdrawal limit, and
 * timestamps for creation and updates. The class uses JPA annotations for ORM mapping and Lombok
 * annotations for boilerplate code reduction
 *
 * @author BankCore Team - Sebastian Orjuela - Cristian Ortiz
 * @version 1.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "accounts",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_account_number", columnNames = "accountNumber")
    },
    indexes = {@Index(name = "idx_account_customer", columnList = "customer_id")})
public class AccountEntity {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @Column(nullable = false, length = 24, updatable = false)
  private String accountNumber;

  @Column(nullable = false, updatable = false)
  private UUID customerId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private AccountType accountType;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CurrencyCode currency;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal balance;

  @Column(nullable = false)
  private String alias;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private AccountStatus status;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal dailyWithdrawalLimit;

  @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant createdAt;

  @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant updatedAt;

  @OneToOne(
      mappedBy = "account",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private AccountPinSecurity security;

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }
}

package com.bankcore.customers.model;

import com.bankcore.customers.utils.enums.CustomerStatus;
import com.bankcore.customers.utils.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing a customer within the system.
 *
 * <p>This entity is mapped to the {@code customers} table and contains authentication, personal,
 * and account-related information.
 *
 * <p>Sensitive fields such as {@code password} and {@code atmPin} must be stored in encrypted or
 * hashed form and must never be exposed outside the persistence layer.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "customers")
public class UserEntity {

  /** Unique identifier of the customer. */
  @Id @GeneratedValue private UUID id;

  /** Government-issued identification number. Must be unique and not null. */
  @Column(unique = true, nullable = false)
  private String dni;

  /** Customer's first name. */
  @Column(nullable = false)
  private String firstName;

  /** Customer's last name. */
  @Column(nullable = false)
  private String lastName;

  /** Customer's email address. Must be unique and not null. */
  @Column(nullable = false, unique = true)
  private String email;

  /**
   * Hashed password used for authentication.
   *
   * <p>Must never be stored in plain text.
   */
  @Column(nullable = false)
  private String password;

  /**
   * Hashed ATM PIN associated with the customer's account.
   *
   * <p>Must be securely stored and never exposed in API responses.
   */
  @Column(nullable = false)
  private String atmPin;

  /** Contact phone number. */
  @Column(nullable = false)
  private String phone;

  /** Residential address of the customer. */
  @Column(nullable = false)
  private String address;

  /**
   * Role assigned to the user for authorization purposes. Stored as a string representation of the
   * enum value.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  /**
   * Current status of the customer account. Stored as a string representation of the enum value.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CustomerStatus status;

  /**
   * Timestamp indicating when the entity was created.
   *
   * <p>This field is automatically populated before persistence and is not updatable.
   */
  @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant createdDate;

  /**
   * Timestamp indicating the last update time of the entity.
   *
   * <p>This field is automatically updated before entity updates.
   */
  @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant updatedDate;

  /**
   * Lifecycle callback executed before the entity is persisted. Initializes creation and update
   * timestamps.
   */
  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    this.createdDate = now;
    this.updatedDate = now;
  }

  /**
   * Lifecycle callback executed before the entity is updated. Updates the modification timestamp.
   */
  @PreUpdate
  protected void onUpdate() {
    this.updatedDate = Instant.now();
  }
}

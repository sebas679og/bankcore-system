package com.bankcore.customers.repository;

import com.bankcore.customers.model.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface responsible for managing persistence operations related to {@link
 * UserEntity}.
 *
 * <p>Extends {@link JpaRepository} to provide standard CRUD operations and defines additional query
 * methods for uniqueness validation.
 *
 * @author BankCore Team - Sebastian Orjuela - Cristian Ortiz
 * @version 1.0
 */
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

  /**
   * Checks whether a user exists with the given DNI.
   *
   * @param dni the government-issued identification number to verify
   * @return {@code true} if a user with the specified DNI exists; {@code false} otherwise
   */
  boolean existsByDni(String dni);

  /**
   * Checks whether a user exists with the given email address.
   *
   * @param email the email address to verify
   * @return {@code true} if a user with the specified email exists; {@code false} otherwise
   */
  boolean existsByEmail(String email);

  /**
   * Retrieves a user entity by email, ignoring case sensitivity.
   *
   * @param email the email address (e.g., "User@Example.com" matches "user@example.com")
   * @return an {@link Optional} containing the {@link UserEntity} if found, or {@link
   *     Optional#empty()} if no user exists with the given email
   */
  Optional<UserEntity> findByEmailIgnoreCase(String email);
}

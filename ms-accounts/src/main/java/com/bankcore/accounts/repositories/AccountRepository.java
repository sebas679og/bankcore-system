package com.bankcore.accounts.repositories;

import com.bankcore.accounts.models.AccountEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing AccountEntity instances in the database. Extends JpaRepository
 * to provide CRUD operations and custom query methods.
 *
 * @author BankCore Team - Sebastian Orjuela - Cristian Ortiz
 * @version 0.1.0
 */
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

  /**
   * Custom query method to count the number of accounts associated with a specific customer ID.
   *
   * @param customerId The UUID of the customer whose accounts are to be counted.
   * @return The number of accounts associated with the given customer ID.
   */
  long countByCustomerId(UUID customerId);

  /**
   * Custom query method to check if an account with a specific alias exists for a given customer
   * ID.
   *
   * @param alias The alias of the account to check for existence.
   * @param customerId The UUID of the customer to whom the account belongs.
   * @return true if an account with the specified alias exists for the given customer ID, false
   *     otherwise.
   */
  boolean existsByAliasAndCustomerId(String alias, UUID customerId);

  /**
   * Custom query method to check if an account with a specific IBAN exists in the database.
   *
   * @param iban The IBAN of the account to check for existence.
   * @return true if an account with the specified IBAN exists, false otherwise.
   */
  boolean existsByAccountNumber(String iban);

  /**
   * Retrieves all bank accounts associated with the given customer ID.
   *
   * <p>Derived query method resolved by Spring Data JPA based on the {@code customerId} field of
   * {@link AccountEntity}.
   *
   * @param id the {@link UUID} of the customer whose accounts are to be retrieved
   * @return a {@link List} of {@link AccountEntity} belonging to the specified customer, or an
   *     empty list if no accounts are found
   */
  List<AccountEntity> findAllByCustomerId(UUID id);

  /**
   * Retrieves an account matching both the given account ID and customer ID.
   *
   * <p>This query enforces ownership validation at the database level, ensuring that a customer can
   * only access accounts that belong to them. Returns an empty {@link Optional} if no match is
   * found, whether the account does not exist or belongs to a different customer — intentionally
   * indistinguishable to prevent account enumeration.
   *
   * @param id the unique identifier of the account
   * @param customerId the unique identifier of the customer who should own the account
   * @return an {@link Optional} containing the matching {@link AccountEntity}, or {@link
   *     Optional#empty()} if no account is found for the given combination
   */
  Optional<AccountEntity> findByIdAndCustomerId(UUID id, UUID customerId);

  /**
   * Finds an account entity by its IBAN (International Bank Account Number).
   *
   * <p>This method queries the persistence layer for an account that matches the provided IBAN. If
   * no account is found, an empty {@link Optional} is returned.
   *
   * @param iban the IBAN string used to identify the account
   * @return an {@link Optional} containing the matching {@link AccountEntity}, or empty if no
   *     account exists with the given IBAN
   */
  Optional<AccountEntity> findByAccountNumber(String iban);

  /**
   * Checks whether a {@link AccountEntity} exists with the given identifiers.
   *
   * <p>This method queries the persistence layer to determine if a transaction with the specified
   * {@code id} and associated {@code customerId} is present. It is typically used for validation or
   * existence checks before performing operations that depend on the transaction's presence.
   *
   * @param id the unique identifier of the transaction
   * @param customerId the unique identifier of the customer associated with the transaction
   * @return {@code true} if a transaction with the given identifiers exists, {@code false}
   *     otherwise
   */
  boolean existsByIdAndCustomerId(UUID id, UUID customerId);
}

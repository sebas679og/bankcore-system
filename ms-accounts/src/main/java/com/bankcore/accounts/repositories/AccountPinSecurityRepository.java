package com.bankcore.accounts.repositories;

import com.bankcore.accounts.models.AccountPinSecurity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing {@link AccountPinSecurity} entities.
 *
 * <p>This interface extends {@link JpaRepository} to provide CRUD operations and custom query
 * methods for PIN security records associated with accounts.
 *
 * @author BankCore Team - Sebastian Orjuela
 * @version 0.1.0
 */
public interface AccountPinSecurityRepository extends JpaRepository<AccountPinSecurity, UUID> {

  /**
   * Finds the {@link AccountPinSecurity} record associated with the given account ID.
   *
   * @param accountId the {@link UUID} representing the account's unique identifier
   * @return an {@link Optional} containing the {@link AccountPinSecurity} record, or empty if no
   *     record exists
   */
  Optional<AccountPinSecurity> findByAccountId(UUID accountId);
}

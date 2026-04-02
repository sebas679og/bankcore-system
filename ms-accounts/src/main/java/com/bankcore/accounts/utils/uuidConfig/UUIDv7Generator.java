package com.bankcore.accounts.utils.uuidConfig;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * Custom identifier generator for Hibernate that produces UUIDv7 values.
 *
 * <p>This implementation leverages {@link UuidCreator#getTimeOrderedEpoch()} to generate
 * time-ordered UUIDs (UUIDv7), which provide both uniqueness and chronological ordering. UUIDv7 is
 * particularly useful for distributed systems where identifiers must be globally unique while still
 * reflecting creation order.
 *
 * <p>Usage:
 *
 * <ul>
 *   <li>Configured as a generator in entity classes using {@code @GeneratedValue(generator =
 *       "uuid7")}.
 *   <li>Ensures that primary keys are unique and sortable by creation time.
 * </ul>
 *
 * <p>Benefits of UUIDv7:
 *
 * <ul>
 *   <li>Globally unique identifiers without requiring a central authority.
 *   <li>Time-ordered values that improve index locality and query performance.
 *   <li>Suitable for high-throughput applications where sequential IDs are not feasible.
 * </ul>
 *
 * @see org.hibernate.id.IdentifierGenerator
 * @see com.github.f4b6a3.uuid.UuidCreator
 * @author BankcoreTeam - Sebastian Orjuela
 * @version 1.0
 */
public class UUIDv7Generator implements IdentifierGenerator {

  /**
   * Generates a new UUIDv7 identifier.
   *
   * <p>The generated UUID is time-ordered, ensuring that newer values are lexicographically greater
   * than older ones. This property improves database index efficiency and supports chronological
   * queries.
   *
   * @param session the Hibernate session context
   * @param object the entity instance for which the identifier is being generated
   * @return a newly generated UUIDv7
   */
  @Override
  public UUID generate(SharedSessionContractImplementor session, Object object) {
    return UuidCreator.getTimeOrderedEpoch();
  }
}

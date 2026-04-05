package com.bankcore.customers;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Abstract base class for integration tests in the Customers microservice. This class sets up the
 * testing environment with Testcontainers and Spring Boot Test configurations.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Starts a PostgreSQL container for database integration testing.
 *   <li>Configures Spring Boot Test with MockMvc for web layer testing.
 *   <li>Activates the "test" profile to use test-specific configurations.
 * </ul>
 *
 * @author BankcoreTeam
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AbstractIntegrationTest {

  /**
   * The PostgreSQL container is defined as a static field to ensure it is shared across all tests
   * and is started only once for the entire test suite. The @ServiceConnection annotation allows
   * Spring Boot to automatically configure the application to connect to this container during
   * tests.
   */
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.8");
}

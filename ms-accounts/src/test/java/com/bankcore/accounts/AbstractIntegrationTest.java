package com.bankcore.accounts;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Abstract base class for integration tests in the accounts module. This class sets up the testing
 * environment with Testcontainers and Spring Boot's testing support.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Starts a PostgreSQL container for database integration testing.
 *   <li>Configures Spring Boot to use the "test" profile for isolated test configurations.
 *   <li>Enables MockMvc for testing web layer interactions without starting a full HTTP server.
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
   * The PostgreSQL container is defined as a static field to ensure it is shared across all test
   * instances, improving performance by avoiding repeated container startups.
   * The @ServiceConnection annotation allows Spring Boot to automatically configure the application
   * context to connect to this container during tests.
   */
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.8");
}

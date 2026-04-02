package com.bankcore.accounts.utils.enums;

/**
 * Defines the security roles available in the system.
 *
 * <p>Roles determine authorization levels and access control to protected resources within the
 * application.
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 1.0
 */
public enum UserRole {

  /** Standard user with access limited to personal operations and customer features. */
  CUSTOMER,

  /**
   * Administrative user with elevated privileges, including management and system configuration
   * capabilities.
   */
  ADMIN,

  /** Internal user used only for queries between microservices */
  SERVICE
}

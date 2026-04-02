package com.bankcore.customers.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.http.HttpHeaders;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 *
 * <p>This class defines the global metadata for the BankCoreSystem-Customers API, including
 * versioning, description, and the security scheme (JWT Bearer Token) required to access protected
 * endpoints.
 *
 * @author BankCore Team - Sebastian Orjuela - Cristian Ortiz
 * @version 1.0
 */
@OpenAPIDefinition(
    info =
        @Info(
            title = "BankCoreSystem-Customers",
            description = "REST API for customer and account management",
            version = "1.0"),
    security = @SecurityRequirement(name = "Security Token"))
@SecurityScheme(
    name = "Security Token",
    description = "Access Token For BankCoreSystem",
    type = SecuritySchemeType.HTTP,
    paramName = HttpHeaders.AUTHORIZATION,
    in = SecuritySchemeIn.HEADER,
    scheme = "bearer",
    bearerFormat = "JWT")
public class OpenApiConfig {
  /**
   * Default constructor for OpenApiConfig. This class is used solely for OpenAPI metadata
   * configuration via annotations.
   */
  public OpenApiConfig() {
    // Empty constructor for documentation purposes
  }
}

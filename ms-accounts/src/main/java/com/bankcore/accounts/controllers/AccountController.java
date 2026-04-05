package com.bankcore.accounts.controllers;

import com.bankcore.accounts.dto.requests.AccountRegisterRequest;
import com.bankcore.accounts.dto.responses.AccountRegisterResponse;
import com.bankcore.accounts.dto.responses.ErrorResponse;
import com.bankcore.accounts.dto.responses.UserAccountDetailResponse;
import com.bankcore.accounts.dto.responses.UserAccountResponse;
import com.bankcore.accounts.services.AccountManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * account service controller.
 *
 * <p>It allows the reception of HTTP requests for the respective management of accounts according
 * to the required service
 *
 * @author Bankcore Team - Sebastian Orjuela - Cristian Ortiz
 * @version 0.1.0
 */
@RestController
@RequestMapping("/api/accounts")
@AllArgsConstructor
@Tag(
    name = "Accounts",
    description = "Controller that handles requests for the management of bank accounts")
public class AccountController {

  private final AccountManagementService accountManagementService;

  /**
   * Verifies that the user is authenticated with the respective role, obtains the name of the token
   * and the JSON body of the request, and returns the corresponding response.
   *
   * @param request the {@link AccountRegisterRequest} contains the data for the creation of the
   *     account
   * @param auth The {@link Authentication} object containing the security context of the user
   * @return a {@link ResponseEntity} that contains the {@link AccountRegisterResponse} along with
   *     its Http code 201
   */
  @Operation(
      summary = "Register a new account",
      description = "Register a new account to the currently authenticated user",
      security = @SecurityRequirement(name = "Security Token"))
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Account registration data",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = AccountRegisterRequest.class)))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "account registered successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AccountRegisterResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error - Invalid input fields",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Authentication credentials were not provided or are invalid",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "The authenticated user does not have permission to access this endpoint.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Conflict - alias already registered",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "422",
        description = "Company policies - The request violates a company policy.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "502",
        description = "Unexpected response received from the Customers service",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "503",
        description = "The Customers service is currently unavailable.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
  })
  @PostMapping()
  public ResponseEntity<AccountRegisterResponse> registerAccount(
      @RequestBody @Valid AccountRegisterRequest request, Authentication auth) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(accountManagementService.registerAccount(request, UUID.fromString(auth.getName())));
  }

  /**
   * Retrieves all accounts associated with the currently authenticated customer.
   *
   * @param auth the authentication object containing the current user's credentials
   * @return a {@link ResponseEntity} containing a list of {@link UserAccountResponse} with HTTP
   *     200, or an {@link ErrorResponse} with the appropriate error code
   */
  @Operation(
      summary = "View Customer accounts",
      description = "Returns the accounts of the authenticated CUSTOMER",
      security = @SecurityRequirement(name = "Security Token"))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Accounts retrieved successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array =
                    @ArraySchema(schema = @Schema(implementation = UserAccountResponse.class)))),
    @ApiResponse(
        responseCode = "401",
        description = "Customer is not authenticated",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Access denied — customer is inactive or does not have the required role",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
  })
  @GetMapping
  public ResponseEntity<List<UserAccountResponse>> getCustomerAccounts(Authentication auth) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(accountManagementService.getCurrentUserAccounts(UUID.fromString(auth.getName())));
  }

  /**
   * Retrieves the full details of a specific account belonging to the authenticated customer.
   *
   * @param accountId the unique identifier of the account to retrieve
   * @param auth the authentication object containing the authenticated customer's JWT claims
   * @return a {@link ResponseEntity} containing the {@link UserAccountDetailResponse} with full
   *     account details
   * @throws com.bankcore.accounts.exceptions.AccountNotFoundException if no account is found for
   *     the given ID and authenticated customer
   */
  @Operation(
      summary = "Get account details",
      description =
          """
             Retrieves the full details of a specific account
             belonging to the authenticated customer.
      """,
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Account details retrieved successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UserAccountDetailResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid account ID format",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - missing or invalid JWT token",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - insufficient permissions to access this resource",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
    @ApiResponse(
        responseCode = "404",
        description = "Account not found",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
  })
  @GetMapping("/{accountId}")
  public ResponseEntity<UserAccountDetailResponse> getAccountDetails(
      @Parameter(description = "Unique identifier of the account to retrieve", required = true)
          @PathVariable
          UUID accountId,
      Authentication auth) {
    return ResponseEntity.ok(
        accountManagementService.getAccountDetails(accountId, UUID.fromString(auth.getName())));
  }
}

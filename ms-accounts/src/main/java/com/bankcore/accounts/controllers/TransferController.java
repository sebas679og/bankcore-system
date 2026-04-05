package com.bankcore.accounts.controllers;

import com.bankcore.accounts.dto.requests.TransferRequest;
import com.bankcore.accounts.dto.responses.ErrorResponse;
import com.bankcore.accounts.dto.responses.TransferResponse;
import com.bankcore.accounts.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for handling transfer operations between accounts.
 *
 * <p>This controller exposes endpoints to initiate money transfers. It delegates the business logic
 * to {@link TransactionService}.
 *
 * <p>All requests require authentication. The authenticated user's identifier is extracted from the
 * {@link Authentication} object.
 *
 * @author BankCore
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transfers")
@Tag(name = "Transfers", description = "Controller that handles transfer requests between accounts")
public class TransferController {

  private final TransactionService transactionService;

  /**
   * Executes a transfer from a source account to a destination account.
   *
   * <p>The method performs the following:
   *
   * <ul>
   *   <li>Validates the incoming request body.
   *   <li>Extracts the authenticated customer ID.
   *   <li>Delegates the transfer operation to {@link TransactionService}.
   * </ul>
   *
   * @param request the {@link TransferRequest} containing transfer details
   * @param auth the {@link Authentication} object containing the authenticated user
   * @return a {@link ResponseEntity} containing the {@link TransferResponse}
   */
  @Operation(
      summary = "Record a Deposit in an account",
      description = "The transfer is captured and recorded in the corresponding accounts",
      security = @SecurityRequirement(name = "Security Token"))
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Data for the transfer between accounts",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = TransferRequest.class)))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Deposit registered in account",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TransferResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - Invalid input fields, incorrect pin",
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
            description =
                """
                    The authenticated user does not have permission to
                    access this endpoint - Client not active in the system
                    """,
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "The resource is not found registered in the system",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - Current account statement not allowed",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "502",
            description =
                "Unexpected response received from customer service - Validation error in system",
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
  @PostMapping
  public ResponseEntity<TransferResponse> transfer(
      @RequestBody @Valid TransferRequest request, Authentication auth) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(transactionService.makeTransfer(request, UUID.fromString(auth.getName())));
  }
}

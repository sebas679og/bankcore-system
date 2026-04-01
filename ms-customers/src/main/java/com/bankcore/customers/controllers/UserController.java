package com.bankcore.customers.controllers;

import com.bankcore.customers.dto.requests.LoginRequest;
import com.bankcore.customers.dto.requests.RegisterRequest;
import com.bankcore.customers.dto.responses.ErrorResponse;
import com.bankcore.customers.dto.responses.LoginResponse;
import com.bankcore.customers.dto.responses.RegisterResponse;
import com.bankcore.customers.services.UserManagement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for handling authentication-related operations.
 *
 * <p>This controller exposes endpoints under {@code /api/auth} for user registration and other
 * authentication workflows.
 *
 * @author BankCore Team - Sebastian Orjuela - Cristian Ortiz
 * @version 0.1.0
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and authentication")
public class UserController {

  private final UserManagement userManagement;

  /**
   * Handles user registration requests.
   *
   * <p>Validates the incoming request payload and delegates the registration process to the
   * application service layer.
   *
   * @param request the registration request containing user credentials and personal data
   * @return a {@link ResponseEntity} containing the registration response with HTTP status {@code
   *     201 Created}
   * @throws org.springframework.web.bind.MethodArgumentNotValidException if the request validation
   *     fails
   */
  @Operation(
      summary = "Register a new user",
      description = "Registers a new user with the provided credentials and personal data.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "User registration data",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = RegisterRequest.class)))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RegisterResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - Invalid input fields",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - Email or DNI already registered",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userManagement.registerCustomer(request));
  }

  /**
   * Authenticates a user based on the provided credentials.
   *
   * <p>This method validates the {@link LoginRequest}, processes the authentication through the
   * user management service, and returns a session token.
   *
   * @param request The login credentials (email and password).
   * @return A {@link ResponseEntity} containing the {@link LoginResponse} with status 200 OK.
   * @throws org.springframework.web.bind.MethodArgumentNotValidException If credentials are invalid
   *     (handled by global exception handler).
   */
  @Operation(
      summary = "User Login",
      description =
          """
              Authenticates a user and returns a JWT token along
              with user uuid, token type and expire time in seconds.
              """)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully authenticated",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials provided",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error or malformed request body",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Parameter(description = "User credentials required for authentication", required = true)
          @Valid
          @RequestBody
          LoginRequest request) {
    return ResponseEntity.status(HttpStatus.OK).body(userManagement.login(request));
  }
}

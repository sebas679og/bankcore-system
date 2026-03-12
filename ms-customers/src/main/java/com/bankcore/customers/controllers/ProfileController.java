package com.bankcore.customers.controllers;

import java.util.UUID;

import com.bankcore.customers.dto.requests.PinValidateRequest;
import com.bankcore.customers.dto.responses.*;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.bankcore.customers.services.UserManagement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing customer profile operations.
 * <p>
 * This controller provides endpoints that allow authenticated customers
 * to retrieve their own profile information from the BankCore system.
 * It integrates with Spring Security to ensure only authorized users
 * with the CUSTOMER role can access the data.
 * </p>
 *
 * @author BankCore Team - Cristian Ortiz - Sebastian Orjuela
 * @version 1.0
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Endpoints for view profiles")
public class ProfileController {

    /**
     * Service layer dependency for user management and business logic.
     */
    private final UserManagement userManagement;

    /**
     * Retrieves the profile information for the currently authenticated customer.
     * <p>
     * This method extracts the username from the {@link Authentication} object
     * and delegates the retrieval to the {@link UserManagement} service.
     * It is restricted to fully authenticated users with the 'CUSTOMER' role.
     * </p>
     *
     * @param auth The {@link Authentication} object containing the security context of the user.
     * @return A {@link ResponseEntity} containing the {@link UserProfileResponse} and HTTP status 200.
     * @see UserManagement#getCurrentUserProfile(String)
     */
    @GetMapping("/me")
    @Operation(
            summary = "View Profile",
            description = "Returns the profile of the authenticated CUSTOMER",
            security = @SecurityRequirement(name = "Security Token"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Profile retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserProfileResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Invalid or missing JWT",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User does not have CUSTOMER role",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Authenticated user not found in database",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<UserProfileResponse> me(Authentication auth) {
        return ResponseEntity.status(HttpStatus.OK).body(userManagement.getCurrentUserProfile(auth.getName()));
    }

    /**
     * Retrieves the details of a specific customer by their unique identifier.
     *
     * <p>This endpoint exposes a REST resource that allows querying detailed
     * information about a customer registered in the system. The customer's
     * identifier is provided as a path variable in the URL.</p>
     *
     * @param customerId UUID representing the unique identifier of the customer.
     * @return ResponseEntity containing a {@link CustomerDetailsValidateResponse}
     *         object with the customer's details, along with HTTP status 200 (OK).
     */
    @GetMapping("/{customerId}")
    @Operation(
            summary = "User details",
            description = "Returns the user's details by their id, only if you are an authenticated ADMIN and SERVICE role",
            security = @SecurityRequirement(name = "Security Token"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User details successfully retrieved",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CustomerDetailsValidateResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation Error - invalid ID parameter",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Invalid or missing JWT",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - The user does not have the SERVICE role",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found - The user does not exist nor is registered in the database",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<CustomerDetailsValidateResponse> getCustomerDetailsById(@PathVariable UUID customerId) {
        return ResponseEntity.status(HttpStatus.OK).body(userManagement.getDetailsCustomer(customerId));
    }

    /**
     * Validates whether a specific customer is active in the system.
     *
     * <p>This endpoint exposes a REST resource that allows checking the status
     * of a customer by their unique identifier. The response indicates whether
     * the customer is currently active or not.</p>
     *
     * @param customerId UUID representing the unique identifier of the customer.
     * @return ResponseEntity containing a {@link CustomerValidateResponse}
     *         object with the validation result of the customer's status,
     *         along with HTTP status 200 (OK).
     */
    @GetMapping("/{customerId}/validate")
    @Operation(
            summary = "User validation",
            description = "Returns existence of the user queried through the id. Allowed only for SERVICE role",
            security = @SecurityRequirement(name = "Security Token"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful return of user existence",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CustomerValidateResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation Error - invalid ID parameter",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Invalid or missing JWT",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - The user does not have the SERVICE role",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<CustomerValidateResponse> getCustomerValidateById(@PathVariable UUID customerId) {
        return ResponseEntity.status(HttpStatus.OK).body(userManagement.getCustomerIsActive(customerId));
    }

    /**
     * Validates the ATM PIN for a specific customer.
     *
     * <p>This endpoint receives a PIN provided by the client and verifies whether it
     * matches the encrypted PIN stored for the given customer. The validation logic
     * is delegated to the service layer.</p>
     *
     * <p>If the customer does not exist, the endpoint does not return an error.
     * Instead, the response indicates that the customer was not found.</p>
     *
     * @param customerId the unique identifier of the customer whose PIN will be validated.
     * @param request the request body containing the PIN to validate.
     * @return a {@link ResponseEntity} containing a {@link PinValidateResponse} with:
     * <ul>
     *     <li>{@code exists}: whether a customer exists with the given {@code customerId}</li>
     *     <li>{@code valid}: whether the provided PIN matches the stored PIN. This value
     *     will be {@code false} if the customer does not exist.</li>
     * </ul>
     */
    @PostMapping("/{customerId}/validate-pin")
    @Operation(
            summary = "PIN validation by client",
            description = "returns if the pin to validate corresponds to the queried user"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Pin to validate",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PinValidateRequest.class)
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns whether the client and the pin are registered",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PinValidateResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error - Pin with invalid format and UUID with bad format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - The user does not have the SERVICE role",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<PinValidateResponse> getPinValidateCustomerById(@PathVariable UUID customerId, @RequestBody @Valid PinValidateRequest request){
        return ResponseEntity.status(HttpStatus.OK).body(userManagement.getPinValidateCustomer(request, customerId));
    }
}

package com.bankcore.accounts.integrations.dto.responses;

/**
 * Response object representing the result of a PIN validation attempt.
 *
 * <p>This Data Transfer Object (DTO) encapsulates whether the provided PIN was valid or not. It is
 * typically returned by authentication or security verification services after checking the user's
 * input.
 *
 * @param valid {@code true} if the PIN is correct, {@code false} otherwise
 * @author Bankcore Team - Sebastian Orjuela
 * @version 0.1.0
 */
public record PinValidateResponse(boolean valid) {}

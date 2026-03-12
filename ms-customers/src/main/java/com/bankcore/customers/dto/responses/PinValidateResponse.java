package com.bankcore.customers.dto.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Response DTO returned by the PIN validation service.
 *
 * <p>This object represents the result of a PIN validation request
 * performed by internal services when authorizing sensitive operations
 * such as financial transactions.</p>
 *
 * <p>The {@code exists} field indicates whether a customer exists
 * with the provided identifier.</p>
 *
 * <p>The {@code valid} field indicates whether the provided PIN
 * matches the encoded PIN stored for the customer. This value will
 * always be {@code false} if the customer does not exist.</p>
 *
 * <p>This response is typically consumed by other internal services
 * to determine whether a secure operation can proceed.</p>
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 1.1
 */
@Setter
@Getter
@Builder
public class PinValidateResponse {

    /**
     * Indicates if the provided client is registered.
     *
     * <p>{@code true} if the client is registered and active,
     * {@code false} otherwise.</p>
     */
    private boolean exists;

    /**
     * Indicates whether the provided PIN is valid.
     *
     * <p>{@code true} if the PIN matches the stored credential,
     * {@code false} otherwise.</p>
     */
    private boolean valid;
}

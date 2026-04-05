package com.bankcore.customers.dto.responses;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object (DTO) for representing standardized error responses. Typically returned when
 * an exception or validation error occurs in the API.
 *
 * @author Bankcore Team
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
@Getter
@Builder
public class ErrorResponse {

  /** HTTP status code of the error (e.g., 404, 500). */
  private final int code;

  /** Name or short identifier of the error (e.g., "NotFound", "InternalServerError"). */
  private final String name;

  /** Detailed description of the error. */
  private final String description;

  /**
   * Timestamp indicating when the error occurred. Defaults to the current instant at the time of
   * object creation.
   */
  @Builder.Default
  private final Instant timestamp = Instant.ofEpochMilli(Instant.now().toEpochMilli());
}

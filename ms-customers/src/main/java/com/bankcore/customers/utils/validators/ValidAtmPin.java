package com.bankcore.customers.utils.validators;

import static java.lang.annotation.ElementType.FIELD;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom Bean Validation annotation used to validate ATM PIN values.
 *
 * <p>This constraint delegates validation logic to {@link AtmPinValidator} and is intended to
 * enforce business rules beyond structural validation such as length or numeric format.
 *
 * <p>Typical usage includes preventing insecure PIN patterns (e.g., all digits being identical).
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 0.1.0
 */
@Documented
@Constraint(validatedBy = AtmPinValidator.class)
@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAtmPin {

  /**
   * Default validation error message returned when the constraint is violated.
   *
   * @return the validation error message
   */
  String message() default "Invalid ATM PIN. All digits cannot be the same";

  /**
   * Allows specification of validation groups.
   *
   * @return the validation groups
   */
  Class<?>[] groups() default {};

  /**
   * Payload that can be attached to the constraint. Typically used by clients of the Bean.
   * Validation API to assign custom metadata to a constraint.
   *
   * @return the payload associated with the constraint
   */
  Class<? extends Payload>[] payload() default {};
}

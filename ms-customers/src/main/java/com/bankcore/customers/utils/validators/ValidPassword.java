package com.bankcore.customers.utils.validators;

import static java.lang.annotation.ElementType.FIELD;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom Bean Validation annotation used to enforce password security requirements.
 *
 * <p>This constraint delegates validation logic to {@link PasswordConstraintValidator}, which
 * applies a configurable set of password complexity rules.
 *
 * <p>The enforced rules typically include:
 *
 * <ul>
 *   <li>Length between 8 and 20 characters
 *   <li>At least one uppercase letter
 *   <li>At least one lowercase letter
 *   <li>At least one numeric digit
 *   <li>At least one special character
 *   <li>No whitespace characters
 * </ul>
 *
 * @author Bankcore Team
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

  /**
   * Default validation error message returned when the password does not satisfy security
   * requirements.
   *
   * @return the validation error message
   */
  String message() default
      """
                  Invalid password. Password must be 8-20 characters
                  long and include at least one uppercase letter,
                  one lowercase letter, one digit, and one special
                  character.
                  """;

  /**
   * Allows specification of validation groups.
   *
   * @return the validation groups
   */
  Class<?>[] groups() default {};

  /**
   * Payload that can be attached to the constraint. Used by Bean Validation clients to associate
   * metadata.
   *
   * @return the payload associated with the constraint
   */
  Class<? extends Payload>[] payload() default {};
}

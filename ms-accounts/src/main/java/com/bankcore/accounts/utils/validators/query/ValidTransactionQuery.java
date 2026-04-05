package com.bankcore.accounts.utils.validators.query;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @ValidTransactionQuery} is a custom validation annotation used to ensure that transaction
 * query parameters comply with defined business rules.
 *
 * <p>This annotation delegates validation logic to {@link TransactionQueryValidator}, which
 * performs checks on the annotated class to guarantee that query parameters are consistent and
 * valid.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Attach validation rules to transaction query objects.
 *   <li>Provide a default validation message when constraints fail.
 *   <li>Support grouping and payload for advanced validation scenarios.
 * </ul>
 *
 * @author BankcoreTeam
 * @author Sebastian Orjuela
 * @version 1.0
 * @see TransactionQueryValidator
 */
@Documented
@Constraint(validatedBy = TransactionQueryValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTransactionQuery {

  /**
   * Default validation message returned when the query parameters are invalid.
   *
   * @return the error message
   */
  String message() default "Invalid transaction query params";

  /**
   * Allows specification of validation groups to apply different validation contexts.
   *
   * @return the validation groups
   */
  Class<?>[] groups() default {};

  /**
   * Can be used by clients of the Bean Validation API to assign custom payload objects to a
   * constraint.
   *
   * @return the payload type
   */
  Class<? extends Payload>[] payload() default {};
}

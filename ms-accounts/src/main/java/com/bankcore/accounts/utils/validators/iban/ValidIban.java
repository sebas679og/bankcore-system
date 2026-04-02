package com.bankcore.accounts.utils.validators.iban;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for International Bank Account Numbers (IBAN).
 *
 * <p>This annotation integrates with Jakarta Bean Validation (JSR 380) and delegates validation
 * logic to {@link IbanValidator}. It is specifically designed to validate Spanish IBANs (country
 * code {@code ES}) according to ISO 13616 rules and MOD 97 checksum validation.
 *
 * @see IbanValidator
 * @author BankcoreTeam - Sebastian Orjuela
 * @version 1.0
 */
@Documented
@Constraint(validatedBy = IbanValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIban {

  /**
   * Default validation error message.
   *
   * @return error message when IBAN is invalid
   */
  String message() default "Invalid IBAN format";

  /**
   * Allows specification of validation groups.
   *
   * @return validation groups
   */
  Class<?>[] groups() default {};

  /**
   * Allows specification of custom payload objects.
   *
   * @return payload classes
   */
  Class<? extends Payload>[] payload() default {};
}

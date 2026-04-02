package com.bankcore.customers.utils.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom validator implementation for the {@link ValidAtmPin} annotation.
 *
 * <p>Validates ATM PIN values according to business rules.
 *
 * <p>Current validation rule:
 *
 * <ul>
 *   <li>Allows null values (nullability is handled separately via {@code @NotNull})
 *   <li>Prevents any digit from appearing more than three times
 * </ul>
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 1.0
 */
public class AtmPinValidator implements ConstraintValidator<ValidAtmPin, String> {

  /**
   * Validates the provided ATM PIN.
   *
   * @param pin the ATM PIN value to validate
   * @param context the constraint validation context
   * @return {@code true} if the PIN satisfies the validation rules; {@code false} otherwise
   */
  @Override
  public boolean isValid(String pin, ConstraintValidatorContext context) {

    // Null values are considered valid here.
    // Nullability must be enforced using @NotNull at field level.
    if (pin == null) {
      return true;
    }

    Map<Character, Integer> frequency = new HashMap<>();

    for (char digit : pin.toCharArray()) {
      frequency.merge(digit, 1, Integer::sum);

      if (frequency.get(digit) > 3) {
        return false;
      }
    }

    return true;
  }
}

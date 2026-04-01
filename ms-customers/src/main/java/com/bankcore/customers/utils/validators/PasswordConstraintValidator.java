package com.bankcore.customers.utils.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.WhitespaceRule;

/**
 * Custom validator implementation for the {@link ValidPassword} annotation.
 *
 * <p>This validator enforces password security policies using a set of predefined rules to ensure
 * strong authentication credentials.
 *
 * <p>The following constraints are applied:
 *
 * <ul>
 *   <li>Minimum length of 8 characters and maximum of 20
 *   <li>At least one uppercase letter
 *   <li>At least one lowercase letter
 *   <li>At least one numeric digit
 *   <li>At least one special character
 *   <li>No whitespace characters allowed
 * </ul>
 *
 * <p>Null or blank values are considered valid in this validator. Presence validation must be
 * handled separately using {@code @NotBlank} or {@code @NotNull}.
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 0.1.0
 */
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

  /** Password validation engine configured with security rules. */
  private final PasswordValidator validator =
      new PasswordValidator(
          List.of(
              new LengthRule(8, 20),
              new CharacterRule(EnglishCharacterData.UpperCase, 1),
              new CharacterRule(EnglishCharacterData.LowerCase, 1),
              new CharacterRule(EnglishCharacterData.Digit, 1),
              new CharacterRule(EnglishCharacterData.Special, 1),
              new WhitespaceRule()));

  /**
   * Validates the provided password against configured security rules.
   *
   * @param password the raw password string to validate
   * @param context the constraint validation context
   * @return {@code true} if the password satisfies all security constraints; {@code false}
   *     otherwise
   */
  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {

    // Null or blank values are handled by @NotBlank or @NotNull
    if (password == null || password.isBlank()) {
      return true;
    }

    return validator.validate(new PasswordData(password)).isValid();
  }
}

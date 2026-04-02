package com.bankcore.accounts.utils.validators.iban;

import com.bankcore.accounts.utils.IbanUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigInteger;

/**
 * Validator for Spanish International Bank Account Numbers (IBAN).
 *
 * <p>Implements {@link ConstraintValidator} to integrate with Jakarta Bean Validation (JSR 380).
 * This validator ensures that an IBAN string complies with the structural and checksum rules
 * defined by ISO 13616 and specific Spanish IBAN requirements.
 *
 * <h2>Validation Rules</h2>
 *
 * <ul>
 *   <li>IBAN must not be {@code null} or blank.
 *   <li>IBAN must be normalized (trimmed, uppercase, no spaces).
 *   <li>Length must equal {@code 24} characters (Spanish IBAN standard).
 *   <li>Must start with country code {@code ES}.
 *   <li>Characters at positions 2–3 must be digits (check digits).
 *   <li>Characters from position 4 onward must be 20 digits (basic bank account number).
 *   <li>Checksum validation: rearrange IBAN, convert letters to numbers, and verify MOD 97 equals
 *       1.
 * </ul>
 *
 * @author BankcoreTeam - Sebastian Orjuela
 * @version 1.0
 */
public class IbanValidator implements ConstraintValidator<ValidIban, String> {

  /** Expected length of a Spanish IBAN (24 characters). */
  private static final int IBAN_ES_LENGTH = 24;

  /** ISO country code for Spain. */
  private static final String COUNTRY_CODE = "ES";

  /**
   * Validates whether the given IBAN string is a valid Spanish IBAN.
   *
   * @param iban the IBAN string to validate
   * @param context validation context provided by Bean Validation
   * @return {@code true} if the IBAN is valid, {@code false} otherwise
   */
  @Override
  public boolean isValid(String iban, ConstraintValidatorContext context) {
    if (iban == null || iban.isBlank()) return false;

    String normalized = iban.trim().toUpperCase().replaceAll("\\s+", "");

    if (normalized.length() != IBAN_ES_LENGTH) return false;
    if (!normalized.startsWith(COUNTRY_CODE)) return false;
    if (!normalized.substring(2, 4).matches("\\d{2}")) return false;
    if (!normalized.substring(4).matches("\\d{20}")) return false;

    String rearranged = normalized.substring(4) + normalized.substring(0, 4);
    String numeric = IbanUtils.convertLettersToNumbers(rearranged);

    return new BigInteger(numeric).mod(BigInteger.valueOf(97)).intValue() == 1;
  }
}

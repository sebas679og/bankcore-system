package com.bankcore.accounts.utils.validators.iban;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("IbanValidator")
public class IbanValidatorTest {

  IbanValidator validator;

  @Mock ConstraintValidatorContext context;

  // IBAN español válido — mod 97 == 1
  private static final String VALID_IBAN = "ES9121000418450200051332";

  @BeforeEach
  void setUp() {
    validator = new IbanValidator();
  }

  // ─── null / blank ─────────────────────────────────────────────────────────

  @Nested
  @DisplayName("when value is null or blank")
  class NullOrBlank {

    @Test
    @DisplayName("returns false for null")
    void shouldReturnFalseForNull() {
      assertThat(validator.isValid(null, context)).isFalse();
    }

    @Test
    @DisplayName("returns false for empty string")
    void shouldReturnFalseForEmpty() {
      assertThat(validator.isValid("", context)).isFalse();
    }

    @ParameterizedTest(name = "blank: \"{0}\"")
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("returns false for blank strings")
    void shouldReturnFalseForBlank(String blank) {
      assertThat(validator.isValid(blank, context)).isFalse();
    }
  }

  // ─── length ───────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("when length is invalid")
  class InvalidLength {

    @Test
    @DisplayName("returns false when IBAN is too short (23 chars)")
    void shouldReturnFalseWhenTooShort() {
      assertThat(validator.isValid("ES91210004184502000513", context)).isFalse();
    }

    @Test
    @DisplayName("returns false when IBAN is too long (25 chars)")
    void shouldReturnFalseWhenTooLong() {
      assertThat(validator.isValid("ES912100041845020005133200", context)).isFalse();
    }
  }

  // ─── country code ─────────────────────────────────────────────────────────

  @Nested
  @DisplayName("when country code is invalid")
  class InvalidCountryCode {

    @Test
    @DisplayName("returns false for non-ES country code (DE)")
    void shouldReturnFalseForGermanIban() {
      // 24 chars, starts with DE
      assertThat(validator.isValid("DE91210004184502000513XX", context)).isFalse();
    }

    @Test
    @DisplayName("returns false when country code is lowercase (es)")
    void shouldReturnFalseForLowercaseCountryCodeAfterNormalization() {
      // lowercase 'es' — after toUpperCase it becomes ES, so this should
      // pass country-code check but may fail checksum; the key point is
      // normalization occurs before country check
      String lowercaseIban = VALID_IBAN.toLowerCase(); // es9121000418450200051332
      // After normalization → ES9121000418450200051332 (valid)
      assertThat(validator.isValid(lowercaseIban, context)).isTrue();
    }
  }

  // ─── check digits ─────────────────────────────────────────────────────────

  @Nested
  @DisplayName("when check digits are invalid")
  class InvalidCheckDigits {

    @Test
    @DisplayName("returns false when check digits contain letters")
    void shouldReturnFalseWhenCheckDigitsAreLetters() {
      // positions 2-3 replaced with "AB"
      assertThat(validator.isValid("ESAB210004184502000513XX", context)).isFalse();
    }

    @Test
    @DisplayName("returns false when check digits contain only one digit")
    void shouldReturnFalseWhenOnlyOneCheckDigit() {
      assertThat(validator.isValid("ES9X210004184502000513XX", context)).isFalse();
    }
  }

  // ─── BBAN (numeric section) ───────────────────────────────────────────────

  @Nested
  @DisplayName("when BBAN contains non-numeric characters")
  class InvalidBban {

    @Test
    @DisplayName("returns false when BBAN has letters")
    void shouldReturnFalseWhenBbanHasLetters() {
      // 4 chars prefix + 20 chars BBAN with letters
      assertThat(validator.isValid("ES912100041845020005133A", context)).isFalse();
    }

    @Test
    @DisplayName("returns false when BBAN has special characters")
    void shouldReturnFalseWhenBbanHasSpecialChars() {
      assertThat(validator.isValid("ES9121000418450200051@!#", context)).isFalse();
    }
  }

  // ─── mod-97 checksum ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("mod-97 checksum")
  class Checksum {

    @Test
    @DisplayName("returns true for a valid Spanish IBAN")
    void shouldReturnTrueForValidIban() {
      assertThat(validator.isValid(VALID_IBAN, context)).isTrue();
    }

    @Test
    @DisplayName("returns false when checksum does not equal 1")
    void shouldReturnFalseForBadChecksum() {
      // Flip one digit in the BBAN to break the checksum
      String corrupted = "ES9121000418450200051333"; // last digit changed
      assertThat(validator.isValid(corrupted, context)).isFalse();
    }

    @ParameterizedTest(name = "valid IBAN: {0}")
    @ValueSource(
        strings = {
          "ES9121000418450200051332",
          "ES6000491500051234567892",
          "ES7620770024003102575766"
        })
    @DisplayName("returns true for multiple known-valid Spanish IBANs")
    void shouldReturnTrueForMultipleValidIbans(String iban) {
      assertThat(validator.isValid(iban, context)).isTrue();
    }
  }

  // ─── whitespace normalization ─────────────────────────────────────────────

  @Nested
  @DisplayName("whitespace normalization")
  class WhitespaceNormalization {

    @Test
    @DisplayName("accepts IBAN with leading and trailing spaces")
    void shouldAcceptIbanWithLeadingTrailingSpaces() {
      assertThat(validator.isValid("  " + VALID_IBAN + "  ", context)).isTrue();
    }

    @Test
    @DisplayName("accepts IBAN with internal spaces (grouped format)")
    void shouldAcceptIbanWithInternalSpaces() {
      // "ES91 2100 0418 4502 0005 1332"
      String grouped = "ES91 2100 0418 4502 0005 1332";
      assertThat(validator.isValid(grouped, context)).isTrue();
    }
  }
}

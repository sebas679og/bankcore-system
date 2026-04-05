package com.bankcore.accounts.utils.validators.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.bankcore.accounts.dto.requests.TransactionQueryParams;
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

/** Unit tests for {@link TransactionQueryValidator}. */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionQueryValidator")
public class TransactionQueryValidatorTest {

  TransactionQueryValidator validator;

  @Mock ConstraintValidatorContext context;
  @Mock ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

  @BeforeEach
  void setUp() {
    validator = new TransactionQueryValidator();

    // Wire the builder chain for every test that triggers addError()
    lenient()
        .when(context.buildConstraintViolationWithTemplate(anyString()))
        .thenReturn(violationBuilder);
    lenient().when(violationBuilder.addConstraintViolation()).thenReturn(context);
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private TransactionQueryParams params(String fromDate, String toDate, String type) {
    TransactionQueryParams p = new TransactionQueryParams();
    p.setFromDate(fromDate);
    p.setToDate(toDate);
    p.setType(type);
    return p;
  }

  // ─── null input ───────────────────────────────────────────────────────────

  @Nested
  @DisplayName("when values is null")
  class NullValues {

    @Test
    @DisplayName("returns true without touching the context")
    void shouldReturnTrueForNull() {
      assertThat(validator.isValid(null, context)).isTrue();
      verifyNoInteractions(context);
    }
  }

  // ─── valid scenarios ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("when params are valid")
  class ValidParams {

    @Test
    @DisplayName("returns true when all fields are null")
    void shouldReturnTrueWhenAllFieldsNull() {
      assertThat(validator.isValid(params(null, null, null), context)).isTrue();
    }

    @Test
    @DisplayName("returns true with only a valid fromDate")
    void shouldReturnTrueWithOnlyFromDate() {
      assertThat(validator.isValid(params("2024-01-01T00:00:00Z", null, null), context)).isTrue();
    }

    @Test
    @DisplayName("returns true with only a valid toDate")
    void shouldReturnTrueWithOnlyToDate() {
      assertThat(validator.isValid(params(null, "2024-12-31T23:59:59Z", null), context)).isTrue();
    }

    @Test
    @DisplayName("returns true when fromDate is before toDate")
    void shouldReturnTrueWhenFromDateIsBeforeToDate() {
      assertThat(
              validator.isValid(
                  params("2024-01-01T00:00:00Z", "2024-12-31T23:59:59Z", null), context))
          .isTrue();
    }

    @Test
    @DisplayName("returns true when fromDate equals toDate")
    void shouldReturnTrueWhenFromDateEqualsToDate() {
      String sameDate = "2024-06-15T12:00:00Z";
      assertThat(validator.isValid(params(sameDate, sameDate, null), context)).isTrue();
    }

    @ParameterizedTest(name = "type: {0}")
    @ValueSource(strings = {"DEPOSIT", "WITHDRAWAL", "deposit", "withdrawal", "Deposit"})
    @DisplayName("returns true for valid transaction types (case-insensitive)")
    void shouldReturnTrueForValidTypes(String type) {
      assertThat(validator.isValid(params(null, null, type), context)).isTrue();
    }

    @Test
    @DisplayName("returns true with all valid fields combined")
    void shouldReturnTrueWithAllValidFields() {
      assertThat(
              validator.isValid(
                  params("2024-01-01T00:00:00Z", "2024-12-31T23:59:59Z", "DEPOSIT"), context))
          .isTrue();
    }
  }

  // ─── fromDate validation ──────────────────────────────────────────────────

  @Nested
  @DisplayName("fromDate validation")
  class FromDateValidation {

    @Test
    @DisplayName("returns false and adds error for invalid fromDate format")
    void shouldReturnFalseForInvalidFromDate() {
      assertThat(validator.isValid(params("not-a-date", null, null), context)).isFalse();
      verify(context).buildConstraintViolationWithTemplate("fromDate must be ISO-8601");
    }

    @ParameterizedTest(name = "fromDate: \"{0}\"")
    @ValueSource(strings = {"2024-01-01", "01/01/2024", "yesterday", "2024-13-01T00:00:00Z"})
    @DisplayName("returns false for various non-ISO-8601 fromDate values")
    void shouldReturnFalseForVariousInvalidFromDates(String fromDate) {
      assertThat(validator.isValid(params(fromDate, null, null), context)).isFalse();
    }
  }

  // ─── toDate validation ────────────────────────────────────────────────────

  @Nested
  @DisplayName("toDate validation")
  class ToDateValidation {

    @Test
    @DisplayName("returns false and adds error for invalid toDate format")
    void shouldReturnFalseForInvalidToDate() {
      assertThat(validator.isValid(params(null, "not-a-date", null), context)).isFalse();
      verify(context).buildConstraintViolationWithTemplate("toDate must be ISO-8601");
    }

    @ParameterizedTest(name = "toDate: \"{0}\"")
    @ValueSource(strings = {"2024-12-31", "31/12/2024", "tomorrow", "2024-00-01T00:00:00Z"})
    @DisplayName("returns false for various non-ISO-8601 toDate values")
    void shouldReturnFalseForVariousInvalidToDates(String toDate) {
      assertThat(validator.isValid(params(null, toDate, null), context)).isFalse();
    }
  }

  // ─── date range ───────────────────────────────────────────────────────────

  @Nested
  @DisplayName("date range validation")
  class DateRangeValidation {

    @Test
    @DisplayName("returns false and adds error when fromDate is after toDate")
    void shouldReturnFalseWhenFromDateIsAfterToDate() {
      assertThat(
              validator.isValid(
                  params("2024-12-31T00:00:00Z", "2024-01-01T00:00:00Z", null), context))
          .isFalse();
      verify(context).buildConstraintViolationWithTemplate("fromDate cannot be after toDate");
    }

    @Test
    @DisplayName("does not add range error when fromDate is invalid (parse failed)")
    void shouldNotAddRangeErrorWhenFromDateIsInvalid() {
      validator.isValid(params("bad-date", "2024-12-31T00:00:00Z", null), context);
      verify(context, never())
          .buildConstraintViolationWithTemplate("fromDate cannot be after toDate");
    }

    @Test
    @DisplayName("does not add range error when toDate is invalid (parse failed)")
    void shouldNotAddRangeErrorWhenToDateIsInvalid() {
      validator.isValid(params("2024-01-01T00:00:00Z", "bad-date", null), context);
      verify(context, never())
          .buildConstraintViolationWithTemplate("fromDate cannot be after toDate");
    }
  }

  // ─── type validation ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("type validation")
  class TypeValidation {

    @Test
    @DisplayName("returns false and adds error for unknown transaction type")
    void shouldReturnFalseForUnknownType() {
      assertThat(validator.isValid(params(null, null, "TRANSFER"), context)).isFalse();
      verify(context).buildConstraintViolationWithTemplate("Invalid transaction type");
    }

    @ParameterizedTest(name = "type: \"{0}\"")
    @ValueSource(strings = {"UNKNOWN", "CREDIT", "DEBIT", "wire", "123"})
    @DisplayName("returns false for various invalid transaction types")
    void shouldReturnFalseForVariousInvalidTypes(String type) {
      assertThat(validator.isValid(params(null, null, type), context)).isFalse();
    }

    @Test
    @DisplayName("skips type validation when type is null")
    void shouldSkipTypeValidationWhenNull() {
      assertThat(validator.isValid(params(null, null, null), context)).isTrue();
      verify(context, never()).buildConstraintViolationWithTemplate("Invalid transaction type");
    }
  }

  // ─── multiple errors ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("multiple simultaneous errors")
  class MultipleErrors {

    @Test
    @DisplayName("accumulates fromDate and toDate errors independently")
    void shouldAccumulateBothDateErrors() {
      validator.isValid(params("bad-from", "bad-to", null), context);

      verify(context).buildConstraintViolationWithTemplate("fromDate must be ISO-8601");
      verify(context).buildConstraintViolationWithTemplate("toDate must be ISO-8601");
    }

    @Test
    @DisplayName("accumulates date and type errors independently")
    void shouldAccumulateDateAndTypeErrors() {
      validator.isValid(params("bad-date", null, "INVALID"), context);

      verify(context).buildConstraintViolationWithTemplate("fromDate must be ISO-8601");
      verify(context).buildConstraintViolationWithTemplate("Invalid transaction type");
    }

    @Test
    @DisplayName("accumulates range and type errors independently")
    void shouldAccumulateRangeAndTypeErrors() {
      validator.isValid(params("2024-12-31T00:00:00Z", "2024-01-01T00:00:00Z", "INVALID"), context);

      verify(context).buildConstraintViolationWithTemplate("fromDate cannot be after toDate");
      verify(context).buildConstraintViolationWithTemplate("Invalid transaction type");
    }

    @Test
    @DisplayName("disables default constraint violation on every call")
    void shouldDisableDefaultConstraintViolation() {
      validator.isValid(params(null, null, null), context);
      verify(context).disableDefaultConstraintViolation();
    }
  }
}

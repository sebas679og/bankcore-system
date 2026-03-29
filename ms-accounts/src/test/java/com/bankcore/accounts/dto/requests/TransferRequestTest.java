package com.bankcore.accounts.dto.requests;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Positive Case: PIN with exactly 4 numeric digits")
    void whenValidPin_thenNoViolations() {
        TransferRequest request = TransferRequest.builder()
                .pin("1234")
                .build();

        Set<ConstraintViolation<TransferRequest>> violations = validator.validateProperty(request, "pin");

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Negative Case (Null): PIN is null")
    void whenNullPin_thenHasViolations() {
        TransferRequest request = TransferRequest.builder()
                .pin(null)
                .build();

        Set<ConstraintViolation<TransferRequest>> violations = validator.validateProperty(request, "pin");

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("cannot be null"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Negative Case (Empty): PIN is an empty string or spaces")
    void whenEmptyPin_thenHasViolations(String pin) {
        TransferRequest request = TransferRequest.builder()
                .pin(pin)
                .build();

        Set<ConstraintViolation<TransferRequest>> violations = validator.validateProperty(request, "pin");

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("cannot be empty"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "12345"})
    @DisplayName("Negative Case (Format): PIN with 3 or 5 digits")
    void whenInvalidLengthPin_thenHasViolations(String pin) {
        TransferRequest request = TransferRequest.builder()
                .pin(pin)
                .build();

        Set<ConstraintViolation<TransferRequest>> violations = validator.validateProperty(request, "pin");

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("exactly 4 numeric digits"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12a4", "1.34", "abcd", "@123"})
    @DisplayName("Negative Case (Characters): PIN containing letters or special characters")
    void whenPinWithInvalidCharacters_thenHasViolations(String pin) {
        TransferRequest request = TransferRequest.builder()
                .pin(pin)
                .build();

        Set<ConstraintViolation<TransferRequest>> violations = validator.validateProperty(request, "pin");

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("exactly 4 numeric digits"));
    }
}

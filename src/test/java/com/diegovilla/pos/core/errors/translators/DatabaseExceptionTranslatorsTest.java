package com.diegovilla.pos.core.errors.translators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.diegovilla.pos.core.errors.exceptions.ApiException;
import java.sql.SQLException;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

class DatabaseExceptionTranslatorsTest {

    private static final String KNOWN_CONSTRAINT = "uk_sample_code";

    /** Feature-agnostic sample exception produced by the stub resolver. */
    private static final class SampleConflictException extends ApiException {
        SampleConflictException() {
            super(HttpStatus.CONFLICT, "Sample already exists.");
        }
    }

    private DatabaseExceptionTranslator translator;

    @BeforeEach
    void setUp() {
        DatabaseExceptionResolver resolver =
                constraint ->
                        KNOWN_CONSTRAINT.equalsIgnoreCase(constraint)
                                ? new SampleConflictException()
                                : null;
        translator = new DatabaseExceptionTranslator(List.of(resolver));
    }

    @Test
    @DisplayName("Should translate a known constraint into its ApiException")
    void shouldTranslateKnownConstraint() {
        ConstraintViolationException cve =
                new ConstraintViolationException("duplicate", new SQLException(), KNOWN_CONSTRAINT);
        DataIntegrityViolationException dive =
                new DataIntegrityViolationException("integrity violation", cve);

        ApiException result = translator.translate(dive);

        assertThat(result).isInstanceOf(SampleConflictException.class);
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Should rethrow the original exception when no resolver matches")
    void shouldRethrowUnknownConstraint() {
        ConstraintViolationException cve =
                new ConstraintViolationException(
                        "duplicate", new SQLException(), "unknown_constraint");
        DataIntegrityViolationException dive =
                new DataIntegrityViolationException("integrity violation", cve);

        assertThatThrownBy(() -> translator.translate(dive)).isSameAs(dive);
    }

    @Test
    @DisplayName(
            "Should rethrow the original exception when the cause is not a constraint violation")
    void shouldRethrowNonConstraintCause() {
        DataIntegrityViolationException dive =
                new DataIntegrityViolationException("integrity violation", new RuntimeException());

        assertThatThrownBy(() -> translator.translate(dive)).isSameAs(dive);
    }
}

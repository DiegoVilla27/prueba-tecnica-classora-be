package com.diegovilla.pos.utils.data;

import com.diegovilla.pos.core.errors.exceptions.DomainException;
import java.util.regex.Pattern;

/**
 * Utility class for validating domain data.
 *
 * <p>Provides reusable validation methods for String values used by domain models during creation.
 * The utility is intentionally independent from DTOs, controllers, persistence, and
 * framework-specific validation mechanisms.
 *
 * @since 1.0.0
 */
public final class ValidateDataUtils {

    /** Utility class; instantiation is not allowed. */
    private ValidateDataUtils() {
        throw new UnsupportedOperationException(
                "ValidateDataUtils is a utility class and cannot be instantiated.");
    }

    /**
     * Validates a required String value.
     *
     * @param value value to validate.
     * @param fieldName field name used in the error message.
     * @return the trimmed value.
     * @throws DomainException if the value is null or blank.
     */
    public static String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainException(String.format("%s is required", fieldName));
        }
        return value.trim();
    }

    /**
     * Validates a required String value and its length.
     *
     * @param value value to validate.
     * @param minLength minimum allowed length.
     * @param maxLength maximum allowed length.
     * @param fieldName field name used in error messages.
     * @return the trimmed and validated value.
     * @throws DomainException if the value is invalid.
     */
    public static String required(String value, int minLength, int maxLength, String fieldName) {
        String trimmed = required(value, fieldName);
        if (trimmed.length() < minLength || trimmed.length() > maxLength) {
            throw new DomainException(
                    String.format(
                            "%s must be between %d and %d characters",
                            fieldName, minLength, maxLength));
        }
        return trimmed;
    }

    /**
     * Validates a required String value against a regular expression.
     *
     * @param value value to validate.
     * @param pattern regular expression used for validation.
     * @param fieldName field name used in error messages.
     * @return the trimmed and validated value.
     * @throws DomainException if the value is invalid.
     */
    public static String required(String value, Pattern pattern, String fieldName) {
        String trimmed = required(value, fieldName);
        if (!pattern.matcher(trimmed).matches()) {
            throw new DomainException(String.format("%s has an invalid format", fieldName));
        }
        return trimmed;
    }
}

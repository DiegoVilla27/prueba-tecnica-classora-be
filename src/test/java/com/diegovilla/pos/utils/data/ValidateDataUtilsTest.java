package com.diegovilla.pos.utils.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.diegovilla.pos.core.errors.exceptions.DomainException;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidateDataUtilsTest {

    @Test
    @DisplayName("Should trim and accept valid values")
    void shouldAcceptValidValues() {
        assertThat(ValidateDataUtils.required(" abc ", "Field")).isEqualTo("abc");
        assertThat(ValidateDataUtils.required(" abc ", 2, 5, "Field")).isEqualTo("abc");
        assertThat(ValidateDataUtils.required("123", Pattern.compile("\\d+"), "Field"))
                .isEqualTo("123");
    }

    @Test
    @DisplayName("Should reject null, too long and mismatching values")
    void shouldRejectInvalidValues() {
        assertThatThrownBy(() -> ValidateDataUtils.required(null, "Field"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Field is required");
        assertThatThrownBy(() -> ValidateDataUtils.required("abcdef", 2, 5, "Field"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Field must be between 2 and 5 characters");
        assertThatThrownBy(
                        () -> ValidateDataUtils.required("abc", Pattern.compile("\\d+"), "Field"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Field has an invalid format");
    }
}

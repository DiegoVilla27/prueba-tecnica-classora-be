package com.diegovilla.pos.features.sale.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.diegovilla.pos.features.sale.domain.exceptions.InvalidQuantityException;
import com.diegovilla.pos.features.sale.domain.valueobjects.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class QuantityTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 999})
    @DisplayName("Should accept the limits 1 and 999")
    void shouldAcceptLimits(int value) {
        assertThat(new Quantity(value).value()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1000})
    @DisplayName("Should reject values outside 1-999")
    void shouldRejectOutOfRange(int value) {
        assertThatThrownBy(() -> new Quantity(value)).isInstanceOf(InvalidQuantityException.class);
    }

    @Test
    @DisplayName("Should add units and reject a sum above 999")
    void shouldAddUnits() {
        assertThat(new Quantity(2).plus(new Quantity(3))).isEqualTo(new Quantity(5));
        assertThatThrownBy(() -> new Quantity(999).plus(new Quantity(1)))
                .isInstanceOf(InvalidQuantityException.class);
    }
}

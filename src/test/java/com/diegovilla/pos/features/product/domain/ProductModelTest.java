package com.diegovilla.pos.features.product.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.diegovilla.pos.features.product.domain.model.ProductModel;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductModelTest {

    @Test
    @DisplayName("Should reconstruct a product from persisted data")
    void shouldReconstruct() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        ProductModel product =
                ProductModel.reconstruct(
                        id, "8410001000011", "Leche entera 1L", new BigDecimal("0.95"), now, now);

        assertThat(product.getId()).isEqualTo(id);
        assertThat(product.getCode()).isEqualTo("8410001000011");
        assertThat(product.getName()).isEqualTo("Leche entera 1L");
        assertThat(product.getPrice()).isEqualByComparingTo("0.95");
        assertThat(product.getCreatedAt()).isEqualTo(now);
        assertThat(product.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should reject a product without price")
    void shouldRejectNullPrice() {
        Instant now = Instant.now();

        assertThatThrownBy(
                        () ->
                                ProductModel.reconstruct(
                                        UUID.randomUUID(),
                                        "8410001000011",
                                        "Leche entera 1L",
                                        null,
                                        now,
                                        now))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Product price cannot be null");
    }
}

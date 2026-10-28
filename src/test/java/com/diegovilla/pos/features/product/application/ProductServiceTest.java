package com.diegovilla.pos.features.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.diegovilla.pos.features.product.application.ports.ProductRepositoryPort;
import com.diegovilla.pos.features.product.application.services.ProductService;
import com.diegovilla.pos.features.product.domain.exceptions.ProductNotFoundException;
import com.diegovilla.pos.features.product.domain.model.ProductModel;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final String CODE = "8410001000011";

    @Mock private ProductRepositoryPort productRepositoryPort;

    @InjectMocks private ProductService productService;

    @Test
    @DisplayName("Should return the product that matches the code")
    void shouldGetByCode() {
        ProductModel product =
                ProductModel.reconstruct(
                        UUID.randomUUID(),
                        CODE,
                        "Leche entera 1L",
                        new BigDecimal("0.95"),
                        Instant.now(),
                        Instant.now());
        when(productRepositoryPort.getByCode(CODE)).thenReturn(Optional.of(product));

        assertThat(productService.getByCode(CODE)).isSameAs(product);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when no product has the code")
    void shouldThrowWhenNotFound() {
        when(productRepositoryPort.getByCode(CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getByCode(CODE))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found.");
    }
}

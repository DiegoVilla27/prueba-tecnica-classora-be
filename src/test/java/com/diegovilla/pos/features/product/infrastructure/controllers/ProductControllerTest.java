package com.diegovilla.pos.features.product.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.diegovilla.pos.features.product.application.services.ProductService;
import com.diegovilla.pos.features.product.domain.model.ProductModel;
import com.diegovilla.pos.features.product.infrastructure.dto.response.ProductResponseDTO;
import com.diegovilla.pos.features.product.infrastructure.mappers.ProductDtoMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private static final String CODE = "8410001000011";

    @Mock private ProductService productService;
    @Mock private ProductDtoMapper productDtoMapper;

    @InjectMocks private ProductController productController;

    @Test
    @DisplayName("Should return 200 with the product found by code")
    void shouldGetByCode() {
        ProductModel product =
                ProductModel.reconstruct(
                        UUID.randomUUID(),
                        CODE,
                        "Leche entera 1L",
                        new BigDecimal("0.95"),
                        Instant.now(),
                        Instant.now());
        ProductResponseDTO dto =
                new ProductResponseDTO(
                        product.getId(), CODE, "Leche entera 1L", new BigDecimal("0.95"));
        when(productService.getByCode(CODE)).thenReturn(product);
        when(productDtoMapper.modelToResponseDTO(product)).thenReturn(dto);

        ResponseEntity<ProductResponseDTO> response = productController.getByCode(CODE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }
}

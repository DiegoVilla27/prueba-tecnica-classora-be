package com.diegovilla.pos.features.product.infrastructure.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.diegovilla.pos.features.product.domain.model.ProductModel;
import com.diegovilla.pos.features.product.infrastructure.entity.ProductEntity;
import com.diegovilla.pos.features.product.infrastructure.mappers.ProductEntityMapper;
import com.diegovilla.pos.features.product.infrastructure.repository.ProductJpaRepository;
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
class ProductRepositoryAdapterTest {

    private static final String CODE = "8410001000011";

    @Mock private ProductJpaRepository productJpaRepository;
    @Mock private ProductEntityMapper productEntityMapper;

    @InjectMocks private ProductRepositoryAdapter adapter;

    @Test
    @DisplayName("Should map the entity found by code")
    void shouldGetByCode() {
        ProductEntity entity = new ProductEntity();
        ProductModel product =
                ProductModel.reconstruct(
                        UUID.randomUUID(),
                        CODE,
                        "Leche entera 1L",
                        new BigDecimal("0.95"),
                        Instant.now(),
                        Instant.now());
        when(productJpaRepository.findByCode(CODE)).thenReturn(Optional.of(entity));
        when(productEntityMapper.entityToModel(entity)).thenReturn(product);

        assertThat(adapter.getByCode(CODE)).contains(product);
    }

    @Test
    @DisplayName("Should return empty when no entity has the code")
    void shouldReturnEmptyWhenNotFound() {
        when(productJpaRepository.findByCode(CODE)).thenReturn(Optional.empty());

        assertThat(adapter.getByCode(CODE)).isEmpty();
    }
}

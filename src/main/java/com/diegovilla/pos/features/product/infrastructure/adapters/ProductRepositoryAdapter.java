package com.diegovilla.pos.features.product.infrastructure.adapters;

import com.diegovilla.pos.features.product.application.ports.ProductRepositoryPort;
import com.diegovilla.pos.features.product.domain.model.ProductModel;
import com.diegovilla.pos.features.product.infrastructure.mappers.ProductEntityMapper;
import com.diegovilla.pos.features.product.infrastructure.repository.ProductJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Persistence adapter implementing {@link ProductRepositoryPort} with Spring Data JPA.
 *
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository productJpaRepository;
    private final ProductEntityMapper productEntityMapper;

    /** {@inheritDoc} */
    @Override
    public Optional<ProductModel> getByCode(String code) {
        return productJpaRepository.findByCode(code).map(productEntityMapper::entityToModel);
    }
}

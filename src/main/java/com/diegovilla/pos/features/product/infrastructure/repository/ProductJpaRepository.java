package com.diegovilla.pos.features.product.infrastructure.repository;

import com.diegovilla.pos.features.product.infrastructure.entity.ProductEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ProductEntity}.
 *
 * @since 1.0.0
 */
@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {

    /**
     * Finds a product by its unique code (backed by the {@code uk_products_code} index).
     *
     * @param code code to search for.
     * @return an {@link Optional} containing the entity if found.
     */
    Optional<ProductEntity> findByCode(String code);
}

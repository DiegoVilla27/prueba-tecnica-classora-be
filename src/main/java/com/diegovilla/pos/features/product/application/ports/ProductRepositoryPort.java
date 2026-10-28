package com.diegovilla.pos.features.product.application.ports;

import com.diegovilla.pos.features.product.domain.model.ProductModel;
import java.util.Optional;

/**
 * Outbound persistence port for {@link ProductModel}.
 *
 * <p>Also used by the {@code sale} feature to resolve scanned codes.
 *
 * @since 1.0.0
 */
public interface ProductRepositoryPort {

    /**
     * Retrieves a product by its scannable code.
     *
     * @param code product code (4–14 digits).
     * @return an {@link Optional} containing the product if found, or empty otherwise.
     */
    Optional<ProductModel> getByCode(String code);
}

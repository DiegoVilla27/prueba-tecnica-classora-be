package com.diegovilla.pos.features.product.application.services;

import com.diegovilla.pos.features.product.application.ports.ProductRepositoryPort;
import com.diegovilla.pos.features.product.domain.exceptions.ProductNotFoundException;
import com.diegovilla.pos.features.product.domain.model.ProductModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service encapsulating the use cases of the product catalog.
 *
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepositoryPort productRepositoryPort;

    /**
     * Retrieves a product by its scannable code.
     *
     * @param code product code (4–14 digits, already validated by the controller).
     * @return the matching {@link ProductModel}.
     * @throws ProductNotFoundException if no product has that code.
     */
    public ProductModel getByCode(String code) {
        ProductModel product =
                productRepositoryPort.getByCode(code).orElseThrow(ProductNotFoundException::new);

        log.info("Product retrieved successfully. id={}", product.getId());

        return product;
    }
}

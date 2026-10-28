package com.diegovilla.pos.features.product.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Domain model representing a catalog product that can be scanned at the checkout.
 *
 * <p>The catalog is read-only for the POS: products are loaded by the Flyway migration {@code
 * V2__seed_products.sql}, so the model only offers {@link #reconstruct}.
 *
 * @since 1.0.0
 */
@Getter
public class ProductModel {

    private final UUID id;
    private final String code;
    private final String name;
    private final BigDecimal price;
    private final Instant createdAt;
    private final Instant updatedAt;

    /** Internal constructor — use the factory method instead. */
    private ProductModel(
            UUID id,
            String code,
            String name,
            BigDecimal price,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "Product ID cannot be null");
        this.code = Objects.requireNonNull(code, "Product code cannot be null");
        this.name = Objects.requireNonNull(name, "Product name cannot be null");
        this.price = Objects.requireNonNull(price, "Product price cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Product createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Product updatedAt cannot be null");
    }

    /**
     * Reconstructs a {@link ProductModel} from persisted data without re-validating business rules
     * (the database constraints already guarantee them).
     *
     * @param id unique identifier.
     * @param code scannable code (4–14 digits).
     * @param name display name.
     * @param price unit price (scale 2, non-negative).
     * @param createdAt creation timestamp.
     * @param updatedAt last-update timestamp.
     * @return a fully hydrated domain model.
     */
    public static ProductModel reconstruct(
            UUID id,
            String code,
            String name,
            BigDecimal price,
            Instant createdAt,
            Instant updatedAt) {
        return new ProductModel(id, code, name, price, createdAt, updatedAt);
    }
}

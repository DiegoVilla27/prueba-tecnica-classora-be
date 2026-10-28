package com.diegovilla.pos.features.product.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing the {@code products} table (schema defined by Flyway {@code V1}).
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class ProductEntity {

    /** Unique primary key identifier. */
    @Id private UUID id;

    /** Unique scannable code (4–14 digits). */
    @Column(nullable = false, length = 14)
    private String code;

    /** Display name. */
    @Column(nullable = false, length = 120)
    private String name;

    /** Unit price. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Timestamp when the record was created. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp when the record was last updated. */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

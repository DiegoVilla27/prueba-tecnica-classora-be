package com.diegovilla.pos.features.sale.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing the {@code sale_lines} table (schema defined by Flyway {@code V1}).
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sale_lines")
public class SaleLineEntity {

    /** Unique primary key identifier. */
    @Id private UUID id;

    /** Sale that owns the line. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false, updatable = false)
    private SaleEntity sale;

    /** Scanned product. */
    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    /** Product code snapshot. */
    @Column(name = "product_code", nullable = false, length = 14, updatable = false)
    private String productCode;

    /** Product name snapshot. */
    @Column(name = "product_name", nullable = false, length = 120, updatable = false)
    private String productName;

    /** Unit price snapshot. */
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2, updatable = false)
    private BigDecimal unitPrice;

    /** Units (1–999). */
    @Column(nullable = false)
    private int quantity;

    /** Scan order inside the sale. */
    @Column(nullable = false)
    private int position;
}

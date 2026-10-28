package com.diegovilla.pos.features.sale.domain.valueobjects;

/**
 * Lifecycle status of a sale.
 *
 * <p>Allowed transitions: {@code OPEN → COMPLETED} and {@code OPEN → CANCELLED}. Only an {@code
 * OPEN} sale can be modified.
 *
 * @since 1.0.0
 */
public enum SaleStatus {
    /** The cashier is scanning products. */
    OPEN,
    /** The sale was finished (charged). */
    COMPLETED,
    /** The sale was discarded ("empty sale" in the checkout). */
    CANCELLED
}

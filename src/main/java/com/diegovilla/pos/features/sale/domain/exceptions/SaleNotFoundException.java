package com.diegovilla.pos.features.sale.domain.exceptions;

import com.diegovilla.pos.core.errors.exceptions.ResourceNotFoundException;

/**
 * Exception thrown when a sale cannot be found (or the cashier has no open sale).
 *
 * <p>Mapped to HTTP {@code 404 Not Found}.
 *
 * @since 1.0.0
 */
public class SaleNotFoundException extends ResourceNotFoundException {

    /** Creates a new exception with a default not-found message. */
    public SaleNotFoundException() {
        super("Sale not found.");
    }
}

package com.diegovilla.pos.features.sale.domain.exceptions;

import com.diegovilla.pos.core.errors.exceptions.ResourceNotFoundException;

/**
 * Exception thrown when a line does not belong to the sale.
 *
 * <p>Mapped to HTTP {@code 404 Not Found}.
 *
 * @since 1.0.0
 */
public class SaleLineNotFoundException extends ResourceNotFoundException {

    /** Creates a new exception with a default not-found message. */
    public SaleLineNotFoundException() {
        super("Sale line not found.");
    }
}

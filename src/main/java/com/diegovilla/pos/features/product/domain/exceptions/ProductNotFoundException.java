package com.diegovilla.pos.features.product.domain.exceptions;

import com.diegovilla.pos.core.errors.exceptions.ResourceNotFoundException;

/**
 * Exception thrown when no product matches the scanned code.
 *
 * <p>Mapped to HTTP {@code 404 Not Found}; the checkout shows it next to the code input.
 *
 * @since 1.0.0
 */
public class ProductNotFoundException extends ResourceNotFoundException {

    /** Creates a new exception with a default not-found message. */
    public ProductNotFoundException() {
        super("Product not found.");
    }
}

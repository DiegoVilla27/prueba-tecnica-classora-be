package com.diegovilla.pos.features.sale.domain.exceptions;

import com.diegovilla.pos.core.errors.exceptions.DomainException;

/**
 * Exception thrown when a line quantity is outside the allowed range (RN-02), also when adding
 * units to an existing line.
 *
 * <p>Mapped to HTTP {@code 422 Unprocessable Content}.
 *
 * @since 1.0.0
 */
public class InvalidQuantityException extends DomainException {

    /** Creates a new exception with the default message. */
    public InvalidQuantityException() {
        super("Quantity must be between 1 and 999.");
    }
}

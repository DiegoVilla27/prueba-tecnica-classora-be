package com.diegovilla.pos.features.sale.domain.exceptions;

import com.diegovilla.pos.core.errors.exceptions.DomainException;

/**
 * Exception thrown when trying to complete a sale without lines (RN-05).
 *
 * <p>Mapped to HTTP {@code 422 Unprocessable Content}.
 *
 * @since 1.0.0
 */
public class EmptySaleException extends DomainException {

    /** Creates a new exception with the default message. */
    public EmptySaleException() {
        super("An empty sale cannot be completed.");
    }
}

package com.diegovilla.pos.features.sale.domain.exceptions;

import com.diegovilla.pos.core.errors.exceptions.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when two requests change the same sale at the same time (RN-10) and the database
 * rejects the second one, e.g. two simultaneous scans of a product not yet in the sale.
 *
 * <p>Mapped to HTTP {@code 409 Conflict}; the client reloads the sale and retries.
 *
 * @since 1.0.0
 */
public class ConcurrentSaleModificationException extends ApiException {

    /** Creates a new exception with the default message. */
    public ConcurrentSaleModificationException() {
        super(
                HttpStatus.CONFLICT,
                "The resource was modified by another request. Reload it and try again.");
    }
}

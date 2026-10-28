package com.diegovilla.pos.features.sale.domain.exceptions;

import com.diegovilla.pos.core.errors.exceptions.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when modifying, completing or cancelling a sale that is no longer open (RN-06).
 *
 * <p>Mapped to HTTP {@code 409 Conflict}.
 *
 * @since 1.0.0
 */
public class SaleNotOpenException extends ApiException {

    /** Creates a new exception with the default message. */
    public SaleNotOpenException() {
        super(HttpStatus.CONFLICT, "The sale is not open.");
    }
}

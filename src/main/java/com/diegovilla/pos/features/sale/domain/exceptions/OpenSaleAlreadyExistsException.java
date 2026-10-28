package com.diegovilla.pos.features.sale.domain.exceptions;

import com.diegovilla.pos.core.errors.exceptions.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a cashier opens a sale while already having an open one (RN-08).
 *
 * <p>Mapped to HTTP {@code 409 Conflict}.
 *
 * @since 1.0.0
 */
public class OpenSaleAlreadyExistsException extends ApiException {

    /** Creates a new exception with the default message. */
    public OpenSaleAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "The cashier already has an open sale.");
    }
}

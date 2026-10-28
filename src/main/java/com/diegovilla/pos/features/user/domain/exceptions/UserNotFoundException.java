package com.diegovilla.pos.features.user.domain.exceptions;

import com.diegovilla.pos.core.errors.exceptions.ResourceNotFoundException;

/**
 * Exception thrown when a cashier cannot be found.
 *
 * <p>Mapped to HTTP {@code 404 Not Found}.
 *
 * @since 1.0.0
 */
public class UserNotFoundException extends ResourceNotFoundException {

    /** Creates a new exception with a default not-found message. */
    public UserNotFoundException() {
        super("User not found.");
    }
}

package com.diegovilla.pos.core.security.jwt.ports;

import java.util.UUID;

/**
 * Port providing access to the currently authenticated cashier identity.
 *
 * <p>Decouples application services from Spring Security context abstractions.
 *
 * @since 1.0.0
 */
public interface AuthenticatedUserProvider {

    /**
     * Resolves the unique identifier of the currently authenticated cashier.
     *
     * @return the {@link UUID} of the authenticated principal.
     */
    UUID getCurrentUserId();
}

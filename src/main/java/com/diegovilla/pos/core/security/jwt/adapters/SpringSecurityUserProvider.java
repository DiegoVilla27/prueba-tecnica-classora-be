package com.diegovilla.pos.core.security.jwt.adapters;

import com.diegovilla.pos.core.security.jwt.ports.AuthenticatedUserProvider;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Spring Security adapter implementing {@link AuthenticatedUserProvider}.
 *
 * <p>Reads the current authentication state from {@link SecurityContextHolder} to extract the
 * authenticated cashier UUID principal.
 *
 * @since 1.0.0
 */
@Component
public class SpringSecurityUserProvider implements AuthenticatedUserProvider {

    /**
     * {@inheritDoc}
     *
     * @throws AccessDeniedException if no authentication exists or user ID is malformed.
     */
    @Override
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new AccessDeniedException("No authenticated user found in security context");
        }

        try {
            // The principal name is the UUID set by JwtAuthenticationFilter
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new AccessDeniedException("Invalid user ID format in security context");
        }
    }
}

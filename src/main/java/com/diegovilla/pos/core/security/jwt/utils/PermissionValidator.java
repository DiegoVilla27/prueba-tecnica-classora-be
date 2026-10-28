package com.diegovilla.pos.core.security.jwt.utils;

import com.diegovilla.pos.core.security.jwt.ports.AuthenticatedUserProvider;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Utility component enforcing ownership-based permission rules.
 *
 * <p>There are no roles: a cashier may only read or modify the resources they own (e.g. their own
 * sales).
 *
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class PermissionValidator {

    private final AuthenticatedUserProvider authenticatedUserProvider;

    /**
     * Validates that the currently authenticated cashier owns the resource.
     *
     * @param ownerId unique identifier of the cashier who owns the resource.
     * @throws AccessDeniedException if the resource belongs to another cashier.
     */
    public void validateHasPermissions(UUID ownerId) {
        if (!authenticatedUserProvider.getCurrentUserId().equals(ownerId)) {
            throw new AccessDeniedException("The resource belongs to another cashier");
        }
    }
}

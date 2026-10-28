package com.diegovilla.pos.features.user.application.ports;

import com.diegovilla.pos.features.user.domain.model.UserModel;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound persistence port for {@link UserModel}.
 *
 * <p>Decouples application logic from concrete database technologies.
 *
 * @since 1.0.0
 */
public interface UserRepositoryPort {

    /**
     * Checks whether a cashier with the given email already exists (case-insensitive).
     *
     * @param email email address to check.
     * @return {@code true} if a matching cashier exists; {@code false} otherwise.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Retrieves a cashier by unique identifier.
     *
     * @param id unique identifier of the cashier.
     * @return an {@link Optional} containing the cashier if found, or empty otherwise.
     */
    Optional<UserModel> getById(UUID id);

    /**
     * Retrieves a cashier by email address (case-insensitive).
     *
     * @param email email address to search for.
     * @return an {@link Optional} containing the cashier if found, or empty otherwise.
     */
    Optional<UserModel> getByEmail(String email);

    /**
     * Persists a new or updated cashier.
     *
     * @param userModel domain model to persist.
     * @return the persisted {@link UserModel}.
     */
    UserModel save(UserModel userModel);
}

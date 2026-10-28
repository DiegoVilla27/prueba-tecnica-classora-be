package com.diegovilla.pos.features.user.infrastructure.repository;

import com.diegovilla.pos.features.user.infrastructure.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link UserEntity}.
 *
 * @since 1.0.0
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Checks whether a user with the specified email exists, ignoring case.
     *
     * @param email email to search for.
     * @return {@code true} if found; {@code false} otherwise.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Finds a user by email, ignoring case.
     *
     * @param email email to search for.
     * @return an {@link Optional} containing the entity if found.
     */
    Optional<UserEntity> findByEmailIgnoreCase(String email);
}

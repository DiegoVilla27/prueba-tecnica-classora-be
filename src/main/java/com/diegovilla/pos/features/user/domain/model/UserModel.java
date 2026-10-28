package com.diegovilla.pos.features.user.domain.model;

import com.diegovilla.pos.utils.data.ValidateDataUtils;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.Getter;

/**
 * Domain aggregate representing a cashier, the only kind of user of the POS.
 *
 * <p>Encapsulates identity, names, email and password hash, plus audit timestamps. There are no
 * roles.
 *
 * @since 1.0.0
 */
@Getter
public class UserModel {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^(?=.{1,120}$)[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String password;
    private final Instant createdAt;
    private final Instant updatedAt;

    /** Internal constructor — use the factory methods instead. */
    private UserModel(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String password,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "User ID cannot be null");
        this.firstName = Objects.requireNonNull(firstName, "User first name cannot be null");
        this.lastName = Objects.requireNonNull(lastName, "User last name cannot be null");
        this.email = Objects.requireNonNull(email, "User email cannot be null");
        this.password = Objects.requireNonNull(password, "User password cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "User createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "User updatedAt cannot be null");
    }

    /**
     * Factory method to create a brand-new cashier.
     *
     * @param firstName first name (2–60 characters).
     * @param lastName last name (2–60 characters).
     * @param email valid email address (stored lower-cased).
     * @param passwordHash already hashed password.
     * @return a new {@link UserModel} with generated UUID and current timestamps.
     * @throws com.diegovilla.pos.core.errors.exceptions.DomainException if any value is invalid.
     */
    public static UserModel create(
            String firstName, String lastName, String email, String passwordHash) {
        Instant now = Instant.now();
        return new UserModel(
                UUID.randomUUID(),
                ValidateDataUtils.required(firstName, 2, 60, "First name"),
                ValidateDataUtils.required(lastName, 2, 60, "Last name"),
                ValidateDataUtils.required(email, EMAIL_PATTERN, "Email").toLowerCase(),
                ValidateDataUtils.required(passwordHash, "Password"),
                now,
                now);
    }

    /**
     * Reconstructs a {@link UserModel} from persisted data without re-validating business rules.
     *
     * @param id unique identifier.
     * @param firstName first name.
     * @param lastName last name.
     * @param email email address.
     * @param password password hash.
     * @param createdAt creation timestamp.
     * @param updatedAt last-update timestamp.
     * @return a fully hydrated domain model.
     */
    public static UserModel reconstruct(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String password,
            Instant createdAt,
            Instant updatedAt) {
        return new UserModel(id, firstName, lastName, email, password, createdAt, updatedAt);
    }
}

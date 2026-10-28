package com.diegovilla.pos.features.user.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing the {@code users} table (schema defined by Flyway {@code V1}).
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    /** Unique primary key identifier. */
    @Id private UUID id;

    /** Cashier first name. */
    @Column(name = "first_name", nullable = false, length = 60)
    private String firstName;

    /** Cashier last name. */
    @Column(name = "last_name", nullable = false, length = 60)
    private String lastName;

    /** Unique email address. */
    @Column(nullable = false, length = 120)
    private String email;

    /** BCrypt password hash. */
    @Column(nullable = false, length = 100)
    private String password;

    /** Timestamp when the record was created. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp when the record was last updated. */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

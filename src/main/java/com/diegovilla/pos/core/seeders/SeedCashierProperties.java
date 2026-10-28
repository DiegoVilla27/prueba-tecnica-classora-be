package com.diegovilla.pos.core.seeders;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Data of the cashier seeded on startup, bound from {@code seed.cashier.*} in {@code
 * application-<profile>.properties}.
 *
 * @param firstName first name.
 * @param lastName last name.
 * @param email login email.
 * @param password plain-text password (hashed before persisting).
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "seed.cashier")
public record SeedCashierProperties(
        String firstName, String lastName, String email, String password) {}

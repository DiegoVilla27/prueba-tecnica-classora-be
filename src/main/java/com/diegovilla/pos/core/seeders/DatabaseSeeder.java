package com.diegovilla.pos.core.seeders;

import com.diegovilla.pos.features.user.application.ports.PasswordHasherPort;
import com.diegovilla.pos.features.user.application.ports.UserRepositoryPort;
import com.diegovilla.pos.features.user.domain.model.UserModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Database seeder that guarantees the demo cashier exists on startup.
 *
 * <p>Idempotent: if a user with the configured email already exists, nothing is done. The product
 * catalog is not seeded here but by Flyway migrations.
 *
 * @since 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SeedCashierProperties.class)
public class DatabaseSeeder {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final SeedCashierProperties seedCashierProperties;

    /**
     * Creates the configured cashier if it does not exist yet.
     *
     * @return a {@link CommandLineRunner} executed after startup.
     */
    @Bean
    CommandLineRunner seedCashier() {
        return args -> {
            if (userRepositoryPort.existsByEmailIgnoreCase(seedCashierProperties.email())) {
                return;
            }

            UserModel cashier =
                    UserModel.create(
                            seedCashierProperties.firstName(),
                            seedCashierProperties.lastName(),
                            seedCashierProperties.email(),
                            passwordHasherPort.hash(seedCashierProperties.password()));

            userRepositoryPort.save(cashier);
            log.info("Seed cashier created successfully. id={}", cashier.getId());
        };
    }
}

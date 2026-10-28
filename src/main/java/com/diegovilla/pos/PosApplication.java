package com.diegovilla.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application entrypoint for the Classora POS (supermarket checkout) API.
 *
 * <p>Bootstraps Spring Boot. Configuration is resolved from {@code application.properties} plus the
 * active profile file ({@code application-local|docker|dev|prod.properties}).
 *
 * @author Diego Villa
 * @since 1.0.0
 */
@SpringBootApplication
public class PosApplication {

    /**
     * Application execution entrypoint.
     *
     * @param args command-line execution arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(PosApplication.class, args);
    }
}

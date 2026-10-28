package com.diegovilla.pos.core.security.cors;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Cross-Origin Resource Sharing (CORS) configuration.
 *
 * <p>Allowed origins come from the {@code cors.allowed-origins} property ({@code
 * CORS_ALLOWED_ORIGINS} environment variable), so each environment only accepts its own front-end
 * (localhost, the Vercel dev domain or the Vercel production domain).
 *
 * @since 1.0.0
 */
@Configuration
public class CorsConfigurationFilter {

    private final List<String> allowedOrigins;

    /**
     * Creates the CORS configuration.
     *
     * @param allowedOrigins comma-separated origin patterns allowed to call the API.
     */
    public CorsConfigurationFilter(@Value("${cors.allowed-origins}") List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * Defines and registers the cross-origin access rules applied across all API endpoints.
     *
     * @return a configured {@link CorsConfigurationSource} mapped to all paths ({@code /**}).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Allowed origins (per environment)
        configuration.setAllowedOriginPatterns(allowedOrigins);

        // 2. Allowed HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));

        // 3. Allowed request headers
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // 4. Headers the front-end may read
        configuration.setExposedHeaders(List.of("Location", "Retry-After"));

        // 5. Credentials are not needed: the JWT travels in the Authorization header
        configuration.setAllowCredentials(false);

        // 6. Preflight (OPTIONS) cache in seconds (1 hour)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

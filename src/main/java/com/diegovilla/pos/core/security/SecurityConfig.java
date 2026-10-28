package com.diegovilla.pos.core.security;

import com.diegovilla.pos.core.security.cors.CorsConfigurationFilter;
import com.diegovilla.pos.core.security.jwt.JwtAuthenticationFilter;
import com.diegovilla.pos.core.security.jwt.JwtProperties;
import com.diegovilla.pos.core.security.ratelimit.RateLimitingFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Spring Security configuration establishing application security filters, stateless session
 * policies, endpoint authorization rules, and CORS/rate limiting integration.
 *
 * <p>There are no roles: every authenticated user is a cashier, so authorization is just {@code
 * authenticated()} plus ownership checks in the services. Registers custom filters for JWT
 * authentication and token-bucket rate limiting.
 *
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    /**
     * Configures the main Spring Security filter chain.
     *
     * @param http the {@link HttpSecurity} builder to configure.
     * @param jwtAuthenticationFilter filter validating JWT bearer tokens on incoming requests.
     * @param rateLimitingFilter filter enforcing token-bucket rate limits per client.
     * @param corsConfigurationFilter CORS source provider for cross-origin web requests.
     * @param resolver Spring MVC exception resolver for delegating 401/403 errors.
     * @return the configured {@link SecurityFilterChain}.
     * @throws Exception if a security configuration error occurs during building.
     */
    @Bean
    @SuppressWarnings({"java:S112", "java:S4502"})
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitingFilter rateLimitingFilter,
            CorsConfigurationFilter corsConfigurationFilter,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver)
            throws Exception {
        http.cors(
                        cors ->
                                cors.configurationSource(
                                        corsConfigurationFilter.corsConfigurationSource()))
                // CSRF is disabled because authentication is stateless via JWT tokens in
                // Authorization headers (no session cookies)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/auth/**", "/api-docs/**", "/actuator/health")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .exceptionHandling(
                        exception ->
                                exception
                                        // 401: delegated to Spring MVC exception handlers
                                        .authenticationEntryPoint(
                                                (request, response, authException) ->
                                                        resolver.resolveException(
                                                                request,
                                                                response,
                                                                null,
                                                                authException))
                                        // 403: delegated to Spring MVC exception handlers
                                        .accessDeniedHandler(
                                                (request, response, accessDeniedException) ->
                                                        resolver.resolveException(
                                                                request,
                                                                response,
                                                                null,
                                                                accessDeniedException)))
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Provides the password encoder bean used for hashing and verifying passwords.
     *
     * @return a {@link BCryptPasswordEncoder} instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

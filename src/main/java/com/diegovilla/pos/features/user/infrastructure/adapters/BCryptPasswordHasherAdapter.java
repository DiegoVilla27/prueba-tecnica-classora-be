package com.diegovilla.pos.features.user.infrastructure.adapters;

import com.diegovilla.pos.features.user.application.ports.PasswordHasherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing {@link PasswordHasherPort} with Spring Security's {@link PasswordEncoder}
 * (BCrypt).
 *
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class BCryptPasswordHasherAdapter implements PasswordHasherPort {

    private final PasswordEncoder passwordEncoder;

    /** {@inheritDoc} */
    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /** {@inheritDoc} */
    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}

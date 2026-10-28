package com.diegovilla.pos.features.auth.application.services;

import com.diegovilla.pos.core.security.jwt.JwtModel;
import com.diegovilla.pos.core.security.jwt.JwtService;
import com.diegovilla.pos.features.auth.application.commands.AuthLoginCommand;
import com.diegovilla.pos.features.auth.application.commands.AuthRefreshCommand;
import com.diegovilla.pos.features.user.application.ports.PasswordHasherPort;
import com.diegovilla.pos.features.user.application.ports.UserRepositoryPort;
import com.diegovilla.pos.features.user.domain.model.UserModel;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service orchestrating cashier authentication and token renewal.
 *
 * <p>There is no public registration: cashiers are provisioned by the seeder.
 *
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid credentials.";
    private static final String INVALID_REFRESH_TOKEN = "Invalid or expired refresh token.";

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final JwtService jwtService;

    /**
     * Authenticates the cashier credentials and issues an access and refresh token pair.
     *
     * <p>The error message is the same whether the email exists or not, so accounts cannot be
     * enumerated.
     *
     * @param command email and plain-text password.
     * @return the issued {@link JwtModel}.
     * @throws BadCredentialsException if the email does not exist or the password does not match.
     */
    public JwtModel login(AuthLoginCommand command) {
        UserModel user =
                userRepositoryPort
                        .getByEmail(command.email())
                        .filter(
                                u ->
                                        passwordHasherPort.matches(
                                                command.password(), u.getPassword()))
                        .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS));

        log.info("User logged in successfully. id={}", user.getId());

        return jwtService.generateToken(user.getId().toString());
    }

    /**
     * Issues a new token pair from a valid refresh token whose cashier still exists.
     *
     * @param command refresh token.
     * @return the renewed {@link JwtModel}.
     * @throws BadCredentialsException if the refresh token is invalid or the cashier no longer
     *     exists.
     */
    public JwtModel refresh(AuthRefreshCommand command) {
        if (!jwtService.isValid(command.refreshToken(), false)) {
            throw new BadCredentialsException(INVALID_REFRESH_TOKEN);
        }

        UUID userId = UUID.fromString(jwtService.extractSubject(command.refreshToken(), false));
        UserModel user =
                userRepositoryPort
                        .getById(userId)
                        .orElseThrow(() -> new BadCredentialsException(INVALID_REFRESH_TOKEN));

        log.info("Session refreshed successfully. id={}", user.getId());

        return jwtService.generateToken(user.getId().toString());
    }
}

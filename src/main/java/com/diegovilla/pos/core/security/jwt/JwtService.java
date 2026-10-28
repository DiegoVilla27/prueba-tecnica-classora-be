package com.diegovilla.pos.core.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service responsible for generating, parsing, and validating JSON Web Tokens (JWT).
 *
 * <p>Handles separate signing keys and expiration periods for short-lived access tokens and
 * long-lived refresh tokens using the HMAC-SHA algorithm. Every token carries a {@code type} claim
 * ({@code access} or {@code refresh}) so one kind can never be used in place of the other.
 *
 * <p>There are no roles: the only user of the system is the cashier.
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    static final String TYPE_CLAIM = "type";
    static final String ACCESS_TYPE = "access";
    static final String REFRESH_TYPE = "refresh";

    private final JwtProperties jwtProperties;

    /**
     * Generates both an Access Token and a Refresh Token packaged into a {@link JwtModel}.
     *
     * @param subject user identifier (UUID string).
     * @return a hydrated {@link JwtModel} containing both tokens and access expiration lifetime.
     */
    public JwtModel generateToken(String subject) {
        Instant now = Instant.now();
        long expSecretSeconds = jwtProperties.expSecret();
        long expRefreshSeconds = jwtProperties.expRefresh();

        String accessToken =
                buildToken(subject, ACCESS_TYPE, now, now.plusSeconds(expSecretSeconds));
        String refreshToken =
                buildToken(subject, REFRESH_TYPE, now, now.plusSeconds(expRefreshSeconds));

        return new JwtModel(accessToken, refreshToken, expSecretSeconds);
    }

    /**
     * Extracts the subject claim (user ID) from a signed JWT token.
     *
     * @param token the JWT token string.
     * @param isAccessToken {@code true} if parsing an access token, {@code false} for a refresh
     *     token.
     * @return the subject extracted from the token claims payload.
     * @throws JwtException if token signature is invalid or expired.
     */
    public String extractSubject(String token, boolean isAccessToken) {
        return parseToken(token, isAccessToken).getPayload().getSubject();
    }

    /**
     * Checks whether a JWT token has a valid signature, is not expired and is of the expected type.
     *
     * @param token the JWT token string to validate.
     * @param isAccessToken {@code true} if validating an access token, {@code false} for a refresh
     *     token.
     * @return {@code true} if the token is valid, active and of the expected type; {@code false}
     *     otherwise.
     */
    public boolean isValid(String token, boolean isAccessToken) {
        try {
            String type =
                    parseToken(token, isAccessToken).getPayload().get(TYPE_CLAIM, String.class);
            return (isAccessToken ? ACCESS_TYPE : REFRESH_TYPE).equals(type);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Builds and signs a token of the given type.
     *
     * @param subject user identifier.
     * @param type token type claim ({@code access} or {@code refresh}).
     * @param issuedAt issue instant.
     * @param expiration expiration instant.
     * @return the compact signed JWT.
     */
    private String buildToken(String subject, String type, Instant issuedAt, Instant expiration) {
        return Jwts.builder()
                .subject(subject)
                .claim(TYPE_CLAIM, type)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey(ACCESS_TYPE.equals(type)))
                .compact();
    }

    /**
     * Parses and cryptographically verifies the claims of a signed JWT token.
     *
     * @param token the JWT token string.
     * @param isAccessToken {@code true} to use the access key, {@code false} for the refresh key.
     * @return the verified {@link Jws} of {@link Claims}.
     */
    private Jws<Claims> parseToken(String token, boolean isAccessToken) {
        return Jwts.parser()
                .verifyWith(getSigningKey(isAccessToken))
                .build()
                .parseSignedClaims(token);
    }

    /**
     * Resolves the HMAC-SHA signing key for access or refresh tokens based on configuration
     * properties.
     *
     * @param isAccessToken {@code true} for access token key, {@code false} for refresh token key.
     * @return a {@link SecretKey} suitable for HMAC signing and verification.
     */
    private SecretKey getSigningKey(boolean isAccessToken) {
        return Keys.hmacShaKeyFor(
                isAccessToken
                        ? jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
                        : jwtProperties.refresh().getBytes(StandardCharsets.UTF_8));
    }
}

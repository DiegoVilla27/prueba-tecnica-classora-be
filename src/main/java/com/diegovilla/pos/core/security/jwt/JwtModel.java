package com.diegovilla.pos.core.security.jwt;

/**
 * Domain representation of issued JSON Web Tokens.
 *
 * <p>Encapsulates both the short-lived access token and long-lived refresh token, along with the
 * access token lifetime in seconds.
 *
 * @param accessToken signed JWT access token for authenticating protected requests.
 * @param refreshToken signed JWT refresh token for renewing expired access tokens.
 * @param expiresIn access token duration in seconds.
 * @since 1.0.0
 */
public record JwtModel(String accessToken, String refreshToken, long expiresIn) {}

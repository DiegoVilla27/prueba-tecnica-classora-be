package com.diegovilla.pos.features.auth.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload returned after a successful login or token refresh.
 *
 * @param accessToken signed JWT access token.
 * @param refreshToken signed JWT refresh token.
 * @param tokenType token type for the {@code Authorization} header (always {@code Bearer}).
 * @param expiresIn access token lifetime in seconds.
 * @since 1.0.0
 */
@Schema(description = "Response body returned after login or token refresh")
public record AuthResponseDTO(
        @Schema(
                        description = "JWT access token for protected requests",
                        example = "eyJhbGciOiJIUzI1NiJ9...")
                String accessToken,
        @Schema(
                        description = "JWT refresh token to obtain new access tokens",
                        example = "eyJhbGciOiJIUzI1NiJ9...")
                String refreshToken,
        @Schema(description = "Token type for the Authorization header", example = "Bearer")
                String tokenType,
        @Schema(description = "Access token lifetime in seconds", example = "3600")
                long expiresIn) {}

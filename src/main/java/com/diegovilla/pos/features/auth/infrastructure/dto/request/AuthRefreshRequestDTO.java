package com.diegovilla.pos.features.auth.infrastructure.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload carrying the refresh token.
 *
 * @param refreshToken signed JWT refresh token.
 * @since 1.0.0
 */
@Schema(description = "Request body required to renew the session")
public record AuthRefreshRequestDTO(
        @Schema(
                        description = "Valid JWT refresh token",
                        example = "eyJhbGciOiJIUzI1NiJ9...",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Refresh token is required")
                String refreshToken) {}

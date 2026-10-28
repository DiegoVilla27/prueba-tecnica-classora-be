package com.diegovilla.pos.features.auth.infrastructure.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload carrying the cashier credentials.
 *
 * @param email registered email address.
 * @param password raw plain-text password.
 * @since 1.0.0
 */
@Schema(description = "Request body required to log in")
public record AuthLoginRequestDTO(
        @Schema(
                        description = "Registered email address of the cashier",
                        example = "cajero@pos.local",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        maxLength = 120)
                @NotBlank(message = "Email is required")
                @Email(message = "Email has to be valid")
                @Size(max = 120, message = "Email must be at most 120 characters")
                String email,
        @Schema(
                        description = "Cashier password",
                        example = "cajero1234",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        maxLength = 72)
                @NotBlank(message = "Password is required")
                @Size(max = 72, message = "Password must be at most 72 characters")
                String password) {}

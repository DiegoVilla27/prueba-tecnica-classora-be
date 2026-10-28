package com.diegovilla.pos.features.user.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * Response DTO with the profile of the currently authenticated cashier.
 *
 * @param id unique identifier of the cashier.
 * @param firstName first name.
 * @param lastName last name.
 * @param email email address.
 * @since 1.0.0
 */
@Schema(description = "Authenticated cashier profile")
public record UserMeResponseDTO(
        @Schema(
                        description = "Unique identifier of the cashier",
                        example = "123e4567-e89b-12d3-a456-426655440000")
                UUID id,
        @Schema(description = "First name of the cashier", example = "Ana") String firstName,
        @Schema(description = "Last name of the cashier", example = "López") String lastName,
        @Schema(description = "Email address of the cashier", example = "cajero@pos.local")
                String email) {}

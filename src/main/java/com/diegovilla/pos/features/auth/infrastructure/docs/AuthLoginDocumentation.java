package com.diegovilla.pos.features.auth.infrastructure.docs;

import com.diegovilla.pos.core.openapi.common.BadRequestApiResponse;
import com.diegovilla.pos.core.openapi.common.InternalServerErrorApiResponse;
import com.diegovilla.pos.core.openapi.common.TooManyRequestsApiResponse;
import com.diegovilla.pos.core.openapi.common.UnauthorizedApiResponse;
import com.diegovilla.pos.features.auth.infrastructure.dto.response.AuthResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Aggregates the OpenAPI metadata for the <em>Log in</em> endpoint.
 *
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SecurityRequirements
@Operation(
        summary = "Log in",
        description =
                "Authenticates the cashier and returns JWT access and refresh tokens. Limited to a few attempts per minute.")
@ApiResponse(
        responseCode = "200",
        description = "Tokens issued successfully.",
        content =
                @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AuthResponseDTO.class)))
@BadRequestApiResponse
@UnauthorizedApiResponse
@TooManyRequestsApiResponse
@InternalServerErrorApiResponse
public @interface AuthLoginDocumentation {}

package com.diegovilla.pos.features.user.infrastructure.docs;

import com.diegovilla.pos.core.openapi.common.InternalServerErrorApiResponse;
import com.diegovilla.pos.core.openapi.common.NotFoundApiResponse;
import com.diegovilla.pos.core.openapi.common.TooManyRequestsApiResponse;
import com.diegovilla.pos.core.openapi.common.UnauthorizedApiResponse;
import com.diegovilla.pos.features.user.infrastructure.dto.response.UserMeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Aggregates the OpenAPI metadata for the <em>Get authenticated cashier</em> endpoint.
 *
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Get authenticated cashier",
        description =
                "Retrieves the profile of the cashier identified by the access token. Shown in the checkout header.")
@ApiResponse(
        responseCode = "200",
        description = "Cashier profile retrieved successfully.",
        content =
                @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UserMeResponseDTO.class)))
@UnauthorizedApiResponse
@NotFoundApiResponse
@TooManyRequestsApiResponse
@InternalServerErrorApiResponse
public @interface GetUserMeDocumentation {}

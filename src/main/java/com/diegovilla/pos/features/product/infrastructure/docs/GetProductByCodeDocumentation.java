package com.diegovilla.pos.features.product.infrastructure.docs;

import com.diegovilla.pos.core.openapi.common.BadRequestApiResponse;
import com.diegovilla.pos.core.openapi.common.InternalServerErrorApiResponse;
import com.diegovilla.pos.core.openapi.common.NotFoundApiResponse;
import com.diegovilla.pos.core.openapi.common.TooManyRequestsApiResponse;
import com.diegovilla.pos.core.openapi.common.UnauthorizedApiResponse;
import com.diegovilla.pos.features.product.infrastructure.dto.response.ProductResponseDTO;
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
 * Aggregates the OpenAPI metadata for the <em>Get product by code</em> endpoint.
 *
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Get product by code",
        description =
                "Retrieves a product by its scannable code (4 to 14 digits). Used by the checkout on every scan.")
@ApiResponse(
        responseCode = "200",
        description = "Product found.",
        content =
                @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ProductResponseDTO.class)))
@BadRequestApiResponse
@UnauthorizedApiResponse
@NotFoundApiResponse
@TooManyRequestsApiResponse
@InternalServerErrorApiResponse
public @interface GetProductByCodeDocumentation {}

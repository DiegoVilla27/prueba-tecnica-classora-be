package com.diegovilla.pos.features.sale.infrastructure.docs;

import com.diegovilla.pos.core.openapi.common.BadRequestApiResponse;
import com.diegovilla.pos.core.openapi.common.ForbiddenApiResponse;
import com.diegovilla.pos.core.openapi.common.InternalServerErrorApiResponse;
import com.diegovilla.pos.core.openapi.common.NotFoundApiResponse;
import com.diegovilla.pos.core.openapi.common.TooManyRequestsApiResponse;
import com.diegovilla.pos.core.openapi.common.UnauthorizedApiResponse;
import com.diegovilla.pos.features.sale.infrastructure.dto.response.SaleResponseDTO;
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
 * Aggregates the OpenAPI metadata for the <em>Get sale by id</em> endpoint.
 *
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Get sale by id",
        description = "Retrieves a sale of the authenticated cashier with its lines and totals.")
@ApiResponse(
        responseCode = "200",
        description = "Sale retrieved.",
        content =
                @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = SaleResponseDTO.class)))
@BadRequestApiResponse
@UnauthorizedApiResponse
@ForbiddenApiResponse
@NotFoundApiResponse
@TooManyRequestsApiResponse
@InternalServerErrorApiResponse
public @interface GetSaleByIdDocumentation {}

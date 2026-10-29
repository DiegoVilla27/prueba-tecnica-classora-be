package com.diegovilla.pos.features.sale.infrastructure.docs;

import com.diegovilla.pos.core.openapi.common.BadRequestApiResponse;
import com.diegovilla.pos.core.openapi.common.InternalServerErrorApiResponse;
import com.diegovilla.pos.core.openapi.common.TooManyRequestsApiResponse;
import com.diegovilla.pos.core.openapi.common.UnauthorizedApiResponse;
import com.diegovilla.pos.features.sale.infrastructure.dto.response.SalePageResponseDTO;
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
 * OpenAPI documentation of {@code GET /sales}.
 *
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "List my sales",
        description =
                "Retrieves the sales history of the authenticated cashier (open, completed and cancelled), newest first and paginated. Each item is a summary; GET /sales/{id} returns the lines.")
@ApiResponse(
        responseCode = "200",
        description = "Page of sales retrieved.",
        content =
                @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = SalePageResponseDTO.class)))
@BadRequestApiResponse
@UnauthorizedApiResponse
@TooManyRequestsApiResponse
@InternalServerErrorApiResponse
public @interface ListSalesDocumentation {}

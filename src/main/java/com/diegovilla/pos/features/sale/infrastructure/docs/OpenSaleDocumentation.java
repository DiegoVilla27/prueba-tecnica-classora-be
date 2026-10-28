package com.diegovilla.pos.features.sale.infrastructure.docs;

import com.diegovilla.pos.core.openapi.common.ConflictApiResponse;
import com.diegovilla.pos.core.openapi.common.InternalServerErrorApiResponse;
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
 * Aggregates the OpenAPI metadata for the <em>Open sale</em> endpoint.
 *
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Open sale",
        description =
                "Opens a new, empty sale for the authenticated cashier. A cashier can have at most one open sale.")
@ApiResponse(
        responseCode = "201",
        description = "Sale opened. The Location header points to the new sale.",
        content =
                @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = SaleResponseDTO.class)))
@UnauthorizedApiResponse
@ConflictApiResponse
@TooManyRequestsApiResponse
@InternalServerErrorApiResponse
public @interface OpenSaleDocumentation {}
